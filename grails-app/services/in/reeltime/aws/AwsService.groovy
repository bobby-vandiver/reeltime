package in.reeltime.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.transfer.TransferManager
import org.springframework.beans.factory.InitializingBean

class AwsService implements InitializingBean {

    private Map<String, Class> interfaceSimpleNamesToAwsClientClasses

    @Override
    void afterPropertiesSet() {
        // TODO: Use AmazonS3EncryptionClient
        interfaceSimpleNamesToAwsClientClasses = [
                AmazonElasticTranscoder: AmazonElasticTranscoderClient,
                AmazonS3: AmazonS3Client
        ]
    }

    def createClient(Class clazz) {
        interfaceSimpleNamesToAwsClientClasses[clazz.simpleName].newInstance()
    }

    def createTransferManager() {
        def s3 = createClient(AmazonS3)
        new TransferManager(s3)
    }
}
