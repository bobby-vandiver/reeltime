package in.reeltime.storage.aws

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.Upload
import grails.test.mixin.TestFor
import in.reeltime.storage.StorageService
import in.reeltime.aws.AwsService
import spock.lang.Specification

@TestFor(S3StorageService)
class S3StorageServiceSpec extends Specification {

    private static final String BUCKET_NAME = 'testBucket'
    private static final String KEY = 'testKey'

    private AmazonS3 mockS3

    void setup() {
        mockS3 = Mock(AmazonS3)
        service.awsService = Stub(AwsService) {
            createClient(AmazonS3) >> mockS3
        }
    }

    void "S3StorageService must be an instance of StorageService"() {
        expect:
        service instanceof StorageService
    }

    void "if the object metadata can be retrieved then the object exists and the path isn't available"() {
        when:
        def available = service.exists(BUCKET_NAME, KEY)

        then:
        available

        and:
        1 * mockS3.getObjectMetadata(BUCKET_NAME, KEY)
    }

    void "getObjectMetadata will throw NoSuchKey error if the requested object doesn't exist"() {
        when:
        def available = service.exists(BUCKET_NAME, KEY)

        then:
        !available

        and:
        1 * mockS3.getObjectMetadata(BUCKET_NAME, KEY) >> { throw new AmazonServiceException('TEST') }
    }

    void "basePath is the bucketName and resourcePath is the key for AWS S3"() {
        given:
        def contents = 'AWS S3 TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        and:
        def mockUpload = Mock(Upload)
        def mockTransferManager = Mock(TransferManager)

        service.awsService = Stub(AwsService) {
            createTransferManager() >> mockTransferManager
        }

        and:
        def validateArgs = { String b, String k, InputStream input, ObjectMetadata metadata ->
            assert b == BUCKET_NAME
            assert k == KEY
            assert input.bytes == contents.bytes
            assert metadata.contentLength == contents.bytes.size()

            return mockUpload
        }

        when:
        service.store(inputStream, BUCKET_NAME, KEY)

        then:
        1 * mockTransferManager.upload(*_) >> { args -> validateArgs(args) }
        1 * mockUpload.waitForUploadResult()
    }
}
