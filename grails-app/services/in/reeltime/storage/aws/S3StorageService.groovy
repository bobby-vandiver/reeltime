package in.reeltime.storage.aws

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import in.reeltime.storage.StorageService

class S3StorageService implements StorageService {

    def awsService

    @Override
    InputStream load(String bucket, String key) {
        return null
    }

    @Override
    boolean exists(String bucket, String key) {
        log.debug("Checking bucket [$bucket] for existence of key [$key]")
        try {
            def s3 = awsService.createClient(AmazonS3) as AmazonS3
            s3.getObjectMetadata(bucket, key)
            return true
        }
        catch (AmazonServiceException ase) {
            // S3 does not expose an API to check for the existence of an object,
            // instead an AmazonServiceException will be thrown if the object does not exist.
            return false
        }
    }

    @Override
    void store(InputStream inputStream, String bucket, String key) {

        log.debug("Storing input stream to bucket [$bucket] with key [$key]")
        def transferManager = awsService.createTransferManager()

        def data = inputStream.bytes
        def metadata = new ObjectMetadata(contentLength: data.size())

        def binaryStream = new ByteArrayInputStream(data)

        def upload = transferManager.upload(bucket, key, binaryStream, metadata)
        upload.waitForUploadResult()
    }
}
