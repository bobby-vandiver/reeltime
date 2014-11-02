package in.reeltime.notification

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import in.reeltime.transcoder.TranscoderJob

import static MessageType.MESSAGE_TYPE_HEADER
import static MessageType.SUBSCRIPTION_CONFIRMATION
import static MessageType.NOTIFICATION

import static javax.servlet.http.HttpServletResponse.*

@Secured(["permitAll"])
class NotificationController {

    def notificationService
    def videoCreationService

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

            def jobId = message.jobId
            def keyPrefix = message.outputKeyPrefix
            def variantPlaylistKey = message.playlists[0].name

            videoCreationService.addPlaylistsToCompletedVideo(jobId, keyPrefix, variantPlaylistKey)
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
        def messageType = request.getHeader(MESSAGE_TYPE_HEADER)

        if(messageType == SUBSCRIPTION_CONFIRMATION) {
            confirmSubscription()
        }
        else if(messageType == NOTIFICATION) {
            notificationHandler()
            render status: SC_OK
        }
        else {
            log.warn("Received an invalid message type: $messageType")
            render status: SC_BAD_REQUEST
        }
    }

    private void confirmSubscription() {
        def topicArn = request.JSON.TopicArn
        def token = request.JSON.Token

        if (topicArn && token) {
            notificationService.confirmSubscription(topicArn, token)
            render status: SC_OK
        }
        else {
            render status: SC_BAD_REQUEST
        }
    }
}
