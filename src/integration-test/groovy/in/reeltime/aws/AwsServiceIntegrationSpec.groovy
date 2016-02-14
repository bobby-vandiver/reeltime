package in.reeltime.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class AwsServiceIntegrationSpec extends Specification {

    @Autowired
    AwsService awsService

    void "must be an InitializingBean"() {
        expect:
        awsService instanceof InitializingBean
    }

    @Unroll
    void "createClient for [#interfaceClass] returns an instance of [#clientClass]"() {
        expect:
        awsService.createClient(interfaceClass).class == clientClass

        where:
        interfaceClass              |   clientClass
        AmazonElasticTranscoder     |   AmazonElasticTranscoderClient
        AmazonS3                    |   AmazonS3Client
        AmazonSNS                   |   AmazonSNSClient
    }
}
