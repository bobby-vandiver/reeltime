package in.reeltime.storage.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3ObjectInputStream
import spock.lang.Specification

class S3ObjectInputStreamWrapperSpec extends Specification {

    void "maintain handle to client"() {
        given:
        def s3 = Stub(AmazonS3)
        def input = Stub(S3ObjectInputStream)

        when:
        def stream = new S3ObjectInputStreamWrapper(input, s3)

        then:
        stream.client == s3
    }
}
