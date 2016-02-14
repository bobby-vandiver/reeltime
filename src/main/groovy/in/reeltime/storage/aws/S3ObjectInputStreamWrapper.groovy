package in.reeltime.storage.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3ObjectInputStream

/*
    This S3ObjectInputStream wrapper maintains a handle to the
    AmazonS3 client to stop garbage collection until after the
    stream has been closed and garbage collected.

    The original implementation of this stream does not keep a
    handle to the client resulting in socket exceptions being
    thrown unexpectedly when the client is garbage collected
    before the stream has been closed.

    Based on the AWS post here:
    https://forums.aws.amazon.com/thread.jspa?messageID=438171
 */
class S3ObjectInputStreamWrapper extends FilterInputStream {

    final AmazonS3 client

    S3ObjectInputStreamWrapper(S3ObjectInputStream inputStream, AmazonS3 client) {
        super(inputStream)
        this.client = client
    }
}
