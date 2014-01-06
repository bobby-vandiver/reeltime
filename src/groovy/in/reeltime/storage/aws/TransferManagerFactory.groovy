package in.reeltime.storage.aws

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.transfer.TransferManager

class TransferManagerFactory {

    static TransferManager create() {
        // TODO: Use encryption client
        def s3 = new AmazonS3Client()
        new TransferManager(s3)
    }
}
