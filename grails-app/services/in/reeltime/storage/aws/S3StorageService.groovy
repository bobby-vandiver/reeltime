package in.reeltime.storage.aws

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import in.reeltime.storage.StorageService

class S3StorageService implements StorageService {

    def awsService

    @Override
    boolean exists(String bucket, String key) {
        log.debug("Checking bucket [$bucket] for existence of key [$key]")
        try {
            def s3 = awsService.createClient(AmazonS3) as AmazonS3
            s3.getObjectMetadata(bucket, key)
            return true
        }
        catch (AmazonServiceException ase) {
            if(ase.errorCode == 'NoSuchKey') {
                return false
            }
            else {
                throw ase
            }
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
