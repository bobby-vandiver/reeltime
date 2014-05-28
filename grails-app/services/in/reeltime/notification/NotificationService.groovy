package in.reeltime.notification

import com.amazonaws.AmazonClientException
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest
import in.reeltime.exceptions.NotificationException

class NotificationService {

    def awsService

    private static final AUTHENTICATE_ON_UNSUBSCRIBE = 'true'

    def confirmSubscription(String topicArn, String token) {
        log.debug("Confirming subscription for topicArn [$topicArn] and token [$token]")
        try {
            def client = awsService.createClient(AmazonSNS) as AmazonSNS
            def confirmRequest = new ConfirmSubscriptionRequest(topicArn, token, AUTHENTICATE_ON_UNSUBSCRIBE)
            client.confirmSubscription(confirmRequest)
        }
        catch(AmazonClientException e) {
            def message = "Failed to confirm subscription for topicArn [$topicArn] and token [$token]"
            throw new NotificationException(message, e)
        }
    }
}
