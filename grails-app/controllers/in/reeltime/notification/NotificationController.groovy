package in.reeltime.notification

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import in.reeltime.exceptions.TranscoderJobNotFoundException

import static MessageType.*
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_OK
import static groovy.json.JsonOutput.toJson

@Secured(["permitAll"])
class NotificationController {

    def notificationService
    def videoCreationService

    def handleMessage() {
        Map message = getMessageAsJson()

        if(isMessageAuthentic(message)) {
            def messageType = request.getHeader(MESSAGE_TYPE_HEADER)

            if (messageType == SUBSCRIPTION_CONFIRMATION) {
                confirmSubscription(message)
            } else if (messageType == NOTIFICATION) {
                handleNotification(message)
            } else {
                log.warn("Received an invalid message type: $messageType")
                response.status = SC_BAD_REQUEST
            }
        }
        else {
            log.warn("Received an unauthenticated message")
            response.status = SC_BAD_REQUEST
        }
    }

    private boolean isMessageAuthentic(Map message) {
        log.debug("Checking message authenticity")
        notificationService.verifyMessage(message)
    }

    private void confirmSubscription(Map message) {
        def topicArn = message.TopicArn
        def token = message.Token

        if (topicArn && token) {
            notificationService.confirmSubscription(topicArn, token)
            render(status: SC_OK)
        }
        else {
            response.status = SC_BAD_REQUEST
        }
    }

    private void handleNotification(Map message) {
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
                log.warn("Received warning notification for job [${jobId}]: ${toJson(message)}")
                break

            case 'ERROR':
                log.error("Received error notification for job [${jobId}]: ${toJson(message)}")
                break

            default:
                log.warn("Received unknown state [$state] for job [${jobId}]: ${toJson(message)}")
        }

        render(status: SC_OK)
    }

    private Map getMessageAsJson() {
        try {
            def slurper = new JsonSlurper()
            def json = slurper.parse(request.inputStream) as Map

            log.debug("Message json: ${json}")
            return slurper.parseText(json.Message) as Map
        }
        catch (Exception e) {
            log.warn("Malformed message", e)
            return [:]
        }
    }
}
