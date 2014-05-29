package in.reeltime.notification

import groovy.json.JsonSlurper
import in.reeltime.transcoder.TranscoderJob

import static MessageType.MESSAGE_TYPE_HEADER
import static MessageType.SUBSCRIPTION_CONFIRMATION
import static MessageType.NOTIFICATION

import static javax.servlet.http.HttpServletResponse.*

class NotificationController {

    def notificationService
    def transcoderJobService
    def playlistService

    // TODO: Implement functional tests to verify this because unit/integration tests can't test this
    // http://jira.grails.org/browse/GRAILS-8426
    static allowedMethods = [completed: 'POST', progressing: 'POST', warning: 'POST', error: 'POST']

    def beforeInterceptor = {
        if(messageIsNotAuthentic()) {
            render status: SC_BAD_REQUEST
            return false
        }
    }

    private boolean messageIsNotAuthentic() {
        def message = request.JSON as Map
        !notificationService.verifyMessage(message)
    }

    def completed() {
        handleRequest {
            def message = messagesAsJson
            def transcoderJob = TranscoderJob.findByJobId(message.jobId)
            transcoderJobService.complete(transcoderJob)

            def video = transcoderJob.video

            def keyPrefix = message.outputKeyPrefix
            def variantPlaylistKey = message.playlists[0].name

            playlistService.addPlaylists(video, keyPrefix, variantPlaylistKey)
        }
    }

    def progressing() {
        handleRequest {
            def message = messagesAsJson
            log.debug("Elastic Transcoder job [${message.jobId}] is progressing")
        }
    }

    private def getMessagesAsJson() {
        def message = request.JSON.Message
        new JsonSlurper().parseText(message)
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

        log.debug(request.JSON)

        if(subscriptionConfirmation) {
            confirmSubscription()
        }
        else if(notification) {
            notificationHandler()
            render status: SC_OK
        }
        else {
            log.warn("Received an invalid message type: $messageTypeHeader")
            render status: SC_BAD_REQUEST
        }
    }

    private boolean isSubscriptionConfirmation() {
        messageTypeHeader == SUBSCRIPTION_CONFIRMATION
    }

    private boolean isNotification() {
        messageTypeHeader == NOTIFICATION
    }

    private String getMessageTypeHeader() {
        request.getHeader(MESSAGE_TYPE_HEADER)
    }

    private void confirmSubscription() {
        def topicArn = topicArn
        def token = token

        if (topicArn && token) {
            notificationService.confirmSubscription(topicArn, token)
            render status: SC_OK
        }
        else {
            render status: SC_BAD_REQUEST
        }
    }

    private String getTopicArn() {
        request.JSON.TopicArn
    }

    private String getToken() {
        request.JSON.Token
    }
}
