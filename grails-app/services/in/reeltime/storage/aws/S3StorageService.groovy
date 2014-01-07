package in.reeltime.storage.aws

import com.amazonaws.services.s3.model.ObjectMetadata
import in.reeltime.storage.StorageService

class S3StorageService implements StorageService {

    @Override
    boolean available(String basePath, String resourcePath) {
        return false
    }

    @Override
    void store(InputStream inputStream, String basePath, String resourcePath) {

        def transferManager = TransferManagerFactory.create()

        def data = inputStream.bytes
        def metadata = new ObjectMetadata(contentLength: data.size())

        def binaryStream = new ByteArrayInputStream(data)

        def upload = transferManager.upload(basePath, resourcePath, binaryStream, metadata)
        upload.waitForUploadResult()
    }
}
