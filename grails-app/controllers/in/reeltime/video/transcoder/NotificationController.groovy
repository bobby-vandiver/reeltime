package in.reeltime.video.transcoder

class NotificationController {

    private static final VALID_TYPES = ['SubscriptionConfirmation', 'Notification']

    // TODO: Implement functional tests to verify this because unit/integration tests can't test this
    // http://jira.grails.org/browse/GRAILS-8426
    static allowedMethods = [completed: 'POST', progressing: 'POST', warning: 'POST', error: 'POST']

    def completed() {
        render status: hasAmzSnsMessageTypeHeader() ? 200 : 400
    }

    def progressing() {
        render status: hasAmzSnsMessageTypeHeader() ? 200 : 400
    }

    def warning() {
        render status: hasAmzSnsMessageTypeHeader() ? 200 : 400
    }

    def error() {
        render status: hasAmzSnsMessageTypeHeader() ? 200 : 400
    }

    private boolean hasAmzSnsMessageTypeHeader() {
        def messageType = request.getHeader('x-amz-sns-message-type')
        log.debug "x-amz-sns-message-type :: $messageType"
        return VALID_TYPES.contains(messageType)
    }
}
