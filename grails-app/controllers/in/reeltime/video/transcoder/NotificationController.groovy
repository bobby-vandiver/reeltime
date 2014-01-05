package in.reeltime.video.transcoder

import static in.reeltime.video.transcoder.aws.sns.MessageType.MESSAGE_TYPE_HEADER
import static in.reeltime.video.transcoder.aws.sns.MessageType.SUBSCRIPTION_CONFIRMATION
import static in.reeltime.video.transcoder.aws.sns.MessageType.NOTIFICATION

class NotificationController {

    def notificationService

    // TODO: Implement functional tests to verify this because unit/integration tests can't test this
    // http://jira.grails.org/browse/GRAILS-8426
    static allowedMethods = [completed: 'POST', progressing: 'POST', warning: 'POST', error: 'POST']

    def completed() {
        handleRequest { render status: 200 }
    }

    def progressing() {
        handleRequest {}
    }

    def warning() {
        handleRequest {
            log.warn(request.inputStream.text)
            render status: 200
        }
    }

    def error() {
        handleRequest {
            log.error(request.inputStream.text)
            render status: 200
        }
    }

    private void handleRequest(Closure notificationHandler) {

        if(subscriptionConfirmation) {
            confirmSubscription()
        }
        else if(notification) {
            notificationHandler()
        }
        else {
            render status: 400
        }
    }

    private boolean isSubscriptionConfirmation() {
        request.getHeader(MESSAGE_TYPE_HEADER) == SUBSCRIPTION_CONFIRMATION
    }

    private boolean isNotification() {
        request.getHeader(MESSAGE_TYPE_HEADER) == NOTIFICATION
    }

    private void confirmSubscription() {
        def url = subscriptionConfirmationUrl

        if (url) {
            notificationService.confirmSubscription(url)
            render status: 200
        }
        else {
            render status: 400
        }
    }

    private String getSubscriptionConfirmationUrl() {
        request.JSON.SubscribeURL
    }
}
