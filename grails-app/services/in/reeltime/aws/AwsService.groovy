package in.reeltime.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sns.AmazonSNSClient
import org.springframework.beans.factory.InitializingBean

class AwsService implements InitializingBean {

    private Map<String, Class> interfaceSimpleNamesToAwsClientClasses

    @Override
    void afterPropertiesSet() {
        // TODO: Use AmazonS3EncryptionClient
        interfaceSimpleNamesToAwsClientClasses = [
                AmazonElasticTranscoder: AmazonElasticTranscoderClient,
                AmazonS3: AmazonS3Client,
                AmazonSNS: AmazonSNSClient
        ].asImmutable()
    }

    def createClient(Class clazz) {
        interfaceSimpleNamesToAwsClientClasses[clazz.simpleName].newInstance()
    }
}
