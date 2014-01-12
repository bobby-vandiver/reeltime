package in.reeltime.storage.aws

import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.Upload
import grails.test.mixin.TestFor
import in.reeltime.storage.StorageService
import in.reeltime.aws.AwsService
import spock.lang.Specification

@TestFor(S3StorageService)
class S3StorageServiceSpec extends Specification {

    void "S3StorageService must be an instance of StorageService"() {
        expect:
        service instanceof StorageService
    }

    void "basePath is the bucketName and resourcePath is the key for AWS S3"() {
        given:
        def bucketName = 'testBucket'
        def key = 'testKey'

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
            assert b == bucketName
            assert k == key
            assert input.bytes == contents.bytes
            assert metadata.contentLength == contents.bytes.size()

            return mockUpload
        }

        when:
        service.store(inputStream, bucketName, key)

        then:
        1 * mockTransferManager.upload(*_) >> { args -> validateArgs(args) }
        1 * mockUpload.waitForUploadResult()
    }
}
