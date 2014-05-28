package in.reeltime.notification

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.AuthorizationErrorException
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest
import com.amazonaws.services.sns.model.InternalErrorException
import com.amazonaws.services.sns.model.NotFoundException
import com.amazonaws.services.sns.model.SubscriptionLimitExceededException
import in.reeltime.aws.AwsService
import in.reeltime.exceptions.NotificationException
import spock.lang.Unroll
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(NotificationService)
class NotificationServiceSpec extends Specification {

    AwsService awsService
    AmazonSNS snsClient

    String topicArn
    String token

    ConfirmSubscriptionRequest confirmationRequest

    void setup() {
        awsService = Mock(AwsService)
        snsClient = Mock(AmazonSNS)
        service.awsService = awsService
        setupConfirmationRequest()
    }

    private void setupConfirmationRequest() {
        topicArn = 'arn:aws:sns:us-east-1:166209233708:ets-listener'
        token = '2336412f37fb687f5d51e6e241d164b051479845a45fd1e10f1287fbc675dba8bb330f79de4343d9bc6e25954e0b9b47c04d6f9d46d7f52460b8f253675f7909d0d801fa1fb7af7aac2400e9491e815b2b506921d04a2a918d70a75f5768b654b6ad6da9bce8c4c98eb6f16857123e51'
        confirmationRequest = new ConfirmSubscriptionRequest(topicArn, token, 'true')
    }

    void "confirm subscription from topicArn and token"() {
        when:
        service.confirmSubscription(topicArn, token)

        then:
        1 * awsService.createClient(AmazonSNS) >> snsClient
        1 * snsClient.confirmSubscription(confirmationRequest)
    }

    @Unroll
    void "SNS client throws exception [#exception.class.simpleName] when confirming request"() {
        when:
        service.confirmSubscription(topicArn, token)

        then:
        def e = thrown(NotificationException)
        e.message == "Failed to confirm subscription for topicArn [$topicArn] and token [$token]"
        e.cause == exception

        and:
        1 * awsService.createClient(AmazonSNS) >> snsClient
        1 * snsClient.confirmSubscription(confirmationRequest) >> { throw exception }

        where:
        _   |   exception
        _   |   new NotFoundException('not found')
        _   |   new AuthorizationErrorException('authorization error')
        _   |   new InternalErrorException('internal error')
        _   |   new SubscriptionLimitExceededException('limit exceeded')
        _   |   new AmazonClientException('client')
        _   |   new AmazonServiceException('aws')
    }
}
