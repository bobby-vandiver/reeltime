import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest

includeTargets << new File("${basedir}/scripts/_Common.groovy")

target(initAwsClients: "Initializes all AWS clients as properties for use") {

    AWSCredentials credentials = loadAWSCredentials()

    if(!hasProperty('s3')) {
        s3 = createS3Client(credentials)
    }

    if(!hasProperty('eb')) {
        eb = createEBClient(credentials)
    }

}

AmazonS3 createS3Client(AWSCredentials credentials) {

    def s3 = new AmazonS3Client(credentials)

    s3.metaClass.objectExists = { String bucket, String key ->
        try {
            delegate.getObjectMetadata(bucket, key)
            return true
        }
        catch (AmazonServiceException ase) {
            // S3 does not expose an API to check for the existence of an object,
            // instead an AmazonServiceException will be thrown if the object does not exist.
            return false
        }
    }

    s3.metaClass.uploadFile = { File file, String bucket, String key ->
        def inputStream = new FileInputStream(file)

        def data = inputStream.bytes
        def totalSize = data.size()

        def metadata = new ObjectMetadata(contentLength: totalSize)
        def binaryStream = new ByteArrayInputStream(data)

        def bytesTransferred = 0

        def request = new PutObjectRequest(bucket, key, binaryStream, metadata)
        request.generalProgressListener = new ProgressListener() {
            @Override
            void progressChanged(ProgressEvent progressEvent) {
                bytesTransferred += progressEvent.bytesTransferred
                displayStatus("Uploaded ${bytesTransferred}/${totalSize} bytes...")
            }
        }

        displayStatus("Uploading file [${file.path}] to S3 bucket [$bucket] with key [$key]")
        delegate.putObject(request)

        displayStatus("Finished uploading file [${file.path}] to S3")

        String statusMessage = "Waiting for [$bucket :: $key] to become available on S3"
        String failureMessage = "A problem occurred while waiting for [$bucket :: $key] on S3"
        long pollingInterval = 5

        waitForCondition(statusMessage, failureMessage, pollingInterval) {
            return s3.objectExists(bucket, key)
        }
    }

    return s3
}

AWSElasticBeanstalk createEBClient(AWSCredentials credentials) {

    def eb = new AWSElasticBeanstalkClient(credentials)

    eb.metaClass.applicationExists = { String applicationName ->
        def application = delegate.describeApplications().applications.find {
            it.applicationName == applicationName
        }
        return application != null
    }

    eb.metaClass.applicationVersionExists = { String applicationName, String version ->
        def applicationVersion = delegate.describeApplicationVersions().applicationVersions.find {
            it.applicationName == applicationName && it.versionLabel == version
        }
        return applicationVersion != null
    }

    eb.metaClass.environmentExists = { String applicationName, String environmentName ->
        def environment = delegate.describeEnvironments().environments.find { EnvironmentDescription env ->
            env.applicationName == applicationName && env.environmentName == environmentName
        }
        return environment != null && environment.status != 'Terminated'
    }

    eb.metaClass.environmentExistsForVersion = { String applicationName, String environmentName, String version ->
        def environment = delegate.describeEnvironments().environments.find { EnvironmentDescription env ->
            env.applicationName == applicationName && env.environmentName == environmentName && env.versionLabel == version
        }
        return environment != null && environment.status != 'Terminated'
    }

    eb.metaClass.environmentIsReady = { String applicationName, String environmentName, String version ->
        def environment = delegate.describeEnvironments().environments.find { EnvironmentDescription env ->
            env.applicationName == applicationName && env.environmentName == environmentName && env.versionLabel == version
        }
        return environment != null && environment.status == 'Ready'
    }

    return eb
}

AWSCredentials loadAWSCredentials() {
    String accessKey = System.properties['AWSAccessKey']
    String secretKey = System.properties['AWSSecretKey']
    new BasicAWSCredentials(accessKey, secretKey)
}