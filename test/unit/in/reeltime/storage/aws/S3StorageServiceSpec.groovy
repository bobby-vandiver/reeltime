package in.reeltime.storage.aws

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
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

    void "load input stream for S3 object"() {
        given:
        def stubS3ObjectInputStream = Stub(S3ObjectInputStream)
        def stubS3Object = Stub(S3Object) {
            getObjectContent() >> stubS3ObjectInputStream
        }

        when:
        def stream = service.load(BUCKET_NAME, KEY)

        then:
        1 * mockS3.getObject(BUCKET_NAME, KEY) >> stubS3Object

        and:
        stream instanceof S3ObjectInputStreamWrapper

        and:
        stream.in == stubS3ObjectInputStream
        stream.client == mockS3
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

    void "store input stream as object specified by the key to an S3 bucket"() {
        given:
        def contents = 'AWS S3 TEST'
        def inputStream = new ByteArrayInputStream(contents.bytes)

        and:
        def validateArgs = { String b, String k, InputStream input, ObjectMetadata metadata ->
            assert b == BUCKET_NAME
            assert k == KEY
            assert input.bytes == contents.bytes
            assert metadata.contentLength == contents.bytes.size()
        }

        when:
        service.store(inputStream, BUCKET_NAME, KEY)

        then:
        1 * mockS3.putObject(*_) >> { args -> validateArgs(args) }
    }
}
