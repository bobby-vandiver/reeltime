package in.reeltime.notification

import groovy.json.JsonSlurper

import static MessageType.MESSAGE_TYPE_HEADER
import static MessageType.SUBSCRIPTION_CONFIRMATION
import static MessageType.NOTIFICATION

import static javax.servlet.http.HttpServletResponse.*

class NotificationController {

    def notificationService
    def transcoderJobService

    // TODO: Implement functional tests to verify this because unit/integration tests can't test this
    // http://jira.grails.org/browse/GRAILS-8426
    static allowedMethods = [completed: 'POST', progressing: 'POST', warning: 'POST', error: 'POST']

    def completed() {
        handleRequest {
            log.info("Elastic Transcoder job [$jobId] is complete")
            transcoderJobService.complete(jobId)
        }
    }

    def progressing() {
        handleRequest {
            log.debug("Elastic Transcoder job [$jobId] is progressing")
        }
    }

    private String getJobId() {
        def message = request.JSON.Message
        new JsonSlurper().parseText(message).jobId
    }

    def warning() {
        handleRequest {
            log.warn(request.inputStream.text)
        }
    }

    def error() {
        handleRequest {
            log.error(request.inputStream.text)
        }
    }

    private void handleRequest(Closure notificationHandler) {

        if(subscriptionConfirmation) {
            confirmSubscription()
        }
        else if(notification) {
            notificationHandler()
            render status: SC_OK
        }
        else {
            render status: SC_BAD_REQUEST
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
            render status: SC_OK
        }
        else {
            render status: SC_BAD_REQUEST
        }
    }

    private String getSubscriptionConfirmationUrl() {
        request.JSON.SubscribeURL
    }
}
