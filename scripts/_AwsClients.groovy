import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceClient
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient

includeTargets << new File("${basedir}/scripts/_Common.groovy")

target(initAwsClients: "Initializes all AWS clients as properties for use") {

    AWSCredentials credentials = loadAWSCredentials()

    if(!hasProperty('s3')) {
        s3 = createS3Client(credentials)
    }

    if(!hasProperty('ec2')) {
        ec2 = createEC2Client(credentials)
    }

    if(!hasProperty('eb')) {
        eb = createEBClient(credentials)
    }

    if(!hasProperty('ets')) {
        ets = createETSClient(credentials)
    }

    if(!hasProperty('sns')) {
        sns = createSNSClient(credentials)
    }

    if(!hasProperty('iam')) {
        iam = createIAMClient(credentials)
    }
}

void wrapAwsClientInvokeMethod(AmazonWebServiceClient client) {

    client.metaClass.invokeMethod { name, args ->
        def clientDelegate = delegate

        return performAwsOperation {
            def metaMethod = clientDelegate.class.metaClass.getMetaMethod(name, args)
            metaMethod?.invoke(clientDelegate, args)
        }
    }
}

Object performAwsOperation(Closure operation) {

    final MAX_RETRIES = 5
    final DELAY_IN_SECS = 5

    int tries = 0
    boolean executed = false

    def result = null

    while(!executed && tries < MAX_RETRIES) {
        try {
            result = operation()
            executed = true
        }
        catch(AmazonClientException ace) {
            tries++

            // Rethrow last failed attempt
            if(tries >= MAX_RETRIES) {
                throw ace
            }

            displayStatus("Caught exception: $ace")
            executed = false

            displayStatus("Waiting ${DELAY_IN_SECS} seconds before retrying request")
            sleep(DELAY_IN_SECS * 1000)
        }
    }
    return result
}

AmazonS3 createS3Client(AWSCredentials credentials) {

    def s3 = new AmazonS3Client(credentials)
    wrapAwsClientInvokeMethod(s3)

    s3.metaClass.bucketExists = { String bucketName ->
        def bucket = delegate.listBuckets().find { it.name == bucketName }
        return bucket != null
    }

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

    def originalDeleteBucket = s3.metaClass.getMetaMethod('deleteBucket', [String] as Class[])
    s3.metaClass.deleteBucket = { String bucketName ->

        delegate.listObjects(bucketName)?.objectSummaries?.each { obj ->
            displayStatus("Deleting object [${obj.key}].")
            delegate.deleteObject(bucketName, obj.key)
        }
        originalDeleteBucket.invoke(delegate, bucketName)
    }

    final long MEGABYTE = 1024L * 1024L

    s3.metaClass.uploadFile = { File file, String bucket, String key ->
        def inputStream = new FileInputStream(file)

        def data = inputStream.bytes
        def totalSize = data.size()

        def metadata = new ObjectMetadata(contentLength: totalSize)
        def binaryStream = new ByteArrayInputStream(data)

        long totalSizeInMB = totalSize / MEGABYTE

        long megaBytesTransferred = 0
        long bytesTransferred = 0

        def request = new PutObjectRequest(bucket, key, binaryStream, metadata)
        request.generalProgressListener = new ProgressListener() {
            @Override
            void progressChanged(ProgressEvent progressEvent) {
                bytesTransferred += progressEvent.bytesTransferred

                if(bytesTransferred > MEGABYTE) {
                    megaBytesTransferred++
                    bytesTransferred = bytesTransferred % MEGABYTE

                    displayStatus("Uploaded ${megaBytesTransferred}MB of ${totalSizeInMB}MB...")
                }

                if(megaBytesTransferred > totalSizeInMB) {
                    displayStatus("Transferred more data than the file contains!")
                    System.exit(1)
                }
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

AmazonEC2 createEC2Client(AWSCredentials credentials) {

    def ec2 = new AmazonEC2Client(credentials)
    wrapAwsClientInvokeMethod(ec2)

    ec2.metaClass.findSecurityGroupsByEnvironmentId = { String environmentId ->
        delegate.describeSecurityGroups().securityGroups.findAll { securityGroup ->
            securityGroup.tags.find { tag -> tag.key == 'elasticbeanstalk:environment-id' }
        }
    }

    ec2.metaClass.findSecurityGroupIdByGroupName = { String groupName ->
        delegate.describeSecurityGroups().securityGroups.find { securityGroup ->
            securityGroup.tags.find { tag -> tag.key == 'Name' && tag.value == groupName }
        }
    }

    ec2.metaClass.findSubnetsByVpcId = { String vpcId ->
        ec2.describeSubnets().subnets.findAll { subnet ->
            subnet.vpcId == vpcId
        }
    }

    ec2.metaClass.findSubnetByVpcIdAndSubnetName = { String vpcId, String subnetName ->
        delegate.findSubnetsByVpcId(vpcId).find { subnet ->
            subnet.tags.find { tag -> tag.key == 'Name' && tag.value == subnetName }
        }
    }

    return ec2
}

AWSElasticBeanstalk createEBClient(AWSCredentials credentials) {

    def eb = new AWSElasticBeanstalkClient(credentials)
    wrapAwsClientInvokeMethod(eb)

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

    eb.metaClass.findEnvironment = { String applicationName, String environmentName ->
        delegate.describeEnvironments().environments.find { EnvironmentDescription env ->
            env.applicationName == applicationName && env.environmentName == environmentName
        }
    }

    eb.metaClass.findEnvironmentByVersion = { String applicationName, String environmentName, String version ->
        delegate.describeEnvironments().environments.find { EnvironmentDescription env ->
            env.applicationName == applicationName && env.environmentName == environmentName && env.versionLabel == version
        }
    }

    eb.metaClass.environmentExists = { String applicationName, String environmentName ->
        def environment = delegate.findEnvironment(applicationName, environmentName)
        return environment != null && environment.status != 'Terminated'
    }

    eb.metaClass.environmentExistsForVersion = { String applicationName, String environmentName, String version ->
        def environment = delegate.findEnvironmentByVersion(applicationName, environmentName, version)
        return environment != null && environment.status != 'Terminated'
    }

    eb.metaClass.environmentIsReady = { String applicationName, String environmentName, String version ->
        def environment = delegate.findEnvironmentByVersion(applicationName, environmentName, version)
        return environment != null && environment.status == 'Ready'
    }

    return eb
}

AmazonElasticTranscoder createETSClient(AWSCredentials credentials) {

    def ets = new AmazonElasticTranscoderClient(credentials)
    wrapAwsClientInvokeMethod(ets)

    ets.metaClass.pipelineExists = { String pipelineName ->
        def pipeline = delegate.listPipelines().pipelines.find { it.name == pipelineName }
        return pipeline != null
    }

    return ets
}

AmazonSNS createSNSClient(AWSCredentials credentials) {

    def sns = new AmazonSNSClient(credentials)
    wrapAwsClientInvokeMethod(sns)

    sns.metaClass.topicExists = { String topicName ->
        def topic = delegate.listTopics().topics.find { it.topicArn.endsWith(topicName) }
        return topic != null
    }

    sns.metaClass.findTopicArnByName = { String topicName ->
        def topic = delegate.listTopics().topics.find { it.topicArn.endsWith(topicName) }
        return topic?.topicArn
    }

    return sns
}

AmazonIdentityManagement createIAMClient(AWSCredentials credentials) {

    def iam  = new AmazonIdentityManagementClient(credentials)
    wrapAwsClientInvokeMethod(iam)

    iam.metaClass.roleExists = { String roleName ->
        def role = delegate.listRoles().roles.find { it.roleName == roleName }
        return role != null
    }

    iam.metaClass.findRoleArnByName = { String roleName ->
        def role = delegate.listRoles().roles.find { it.roleName == roleName }
        return role.arn
    }

    iam.metaClass.findServerCertificateArnByName = { String certificateName ->
        def certificate = delegate.listServerCertificates().serverCertificateMetadataList.find {
            it.serverCertificateName == certificateName
        }
        return certificate.arn
    }

    return iam
}

AWSCredentials loadAWSCredentials() {
    String accessKey = System.properties['AWSAccessKey']
    String secretKey = System.properties['AWSSecretKey']
    new BasicAWSCredentials(accessKey, secretKey)
}