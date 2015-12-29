package in.reeltime.notification

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import in.reeltime.exceptions.TranscoderJobNotFoundException

import static MessageType.*
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_OK

@Secured(["permitAll"])
class NotificationController {

    def notificationService
    def videoCreationService

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

    def handleMessage() {
        log.debug(request.JSON)
        def messageType = request.getHeader(MESSAGE_TYPE_HEADER)

        if(messageType == SUBSCRIPTION_CONFIRMATION) {
            confirmSubscription()
        }
        else if(messageType == NOTIFICATION) {
            handleNotification()
        }
        else {
            log.warn("Received an invalid message type: $messageType")
            render(status: SC_BAD_REQUEST)
        }
    }

    private void confirmSubscription() {
        def topicArn = request.JSON.TopicArn
        def token = request.JSON.Token

        if (topicArn && token) {
            notificationService.confirmSubscription(topicArn, token)
            render(status: SC_OK)
        }
        else {
            render(status: SC_BAD_REQUEST)
        }
    }

    private void handleNotification() {
        def message = messageAsJson

        def state = message.state
        def jobId = message.jobId

        switch(state) {
            case 'COMPLETED':
                try {
                    def keyPrefix = message.outputKeyPrefix
                    def variantPlaylistKey = message.playlists[0].name

                    videoCreationService.completeVideoCreation(jobId, keyPrefix, variantPlaylistKey)
                }
                catch(TranscoderJobNotFoundException e) {
                    log.warn("Could not find transcoder job [$jobId] -- assuming it was already removed")
                }
                break

            case 'PROGRESSING':
                log.debug("Elastic Transcoder job [${jobId}] is progressing")
                break

            case 'WARNING':
                log.warn("Received warning notification for job [${jobId}]: ${request.inputStream.text}")
                break

            case 'ERROR':
                log.error("Received error notification for job [${jobId}]: ${request.inputStream.text}")
                break

            default:
                log.warn("Received unknown state [$state] for job [${jobId}]: ${request.inputStream.text}")
        }

        render(status: SC_OK)
    }

    private def getMessageAsJson() {
        def message = request.JSON.Message
        new JsonSlurper().parseText(message)
    }
}
