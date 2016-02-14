package in.reeltime.deployment.aws.client

import com.amazonaws.AmazonServiceException
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3ObjectSummary

import static in.reeltime.deployment.log.StatusLogger.*
import static in.reeltime.deployment.condition.ConditionalWait.*

class EnhancedAmazonS3 implements AmazonS3 {

    private final static long MEGABYTE = 1024L * 1024L

    @Delegate
    AmazonS3 amazonS3

    EnhancedAmazonS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3
    }

    boolean bucketExists(String bucketName) {
        return listBuckets().find { it.name == bucketName } != null
    }

    boolean objectExists(String bucket, String key) {
        try {
            getObjectMetadata(bucket, key)
            return true
        }
        catch (AmazonServiceException ase) {
            // S3 does not expose an API to check for the existence of an object,
            // instead an AmazonServiceException will be thrown if the object does not exist.
            return false
        }
    }

    @Override
    void deleteBucket(String bucketName) {
        List<S3ObjectSummary> objects = listObjects(bucketName)?.objectSummaries

        objects.each { obj ->
            displayStatus("Deleting object [${obj.key}].")
            deleteObject(bucketName, obj.key)
        }

        amazonS3.deleteBucket(bucketName)
    }

    void uploadFile(File file, String bucket, String key) {
        def inputStream = new FileInputStream(file)

        long totalSize = file.length()
        long totalSizeInMB = totalSize / MEGABYTE

        def metadata = new ObjectMetadata(contentLength: totalSize)

        long megaBytesTransferred = 0
        long bytesTransferred = 0

        def request = new PutObjectRequest(bucket, key, inputStream, metadata)
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
        putObject(request)

        displayStatus("Finished uploading file [${file.path}] to S3")

        String statusMessage = "Waiting for [$bucket :: $key] to become available on S3"
        String failureMessage = "A problem occurred while waiting for [$bucket :: $key] on S3"
        long pollingInterval = 5

        waitForCondition(statusMessage, failureMessage, pollingInterval) {
            return objectExists(bucket, key)
        }
    }
}
