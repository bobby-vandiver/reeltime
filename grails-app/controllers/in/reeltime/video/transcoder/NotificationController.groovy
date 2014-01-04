package in.reeltime.video.transcoder

class NotificationController {

    private static final VALID_TYPES = ['SubscriptionConfirmation', 'Notification']

    // TODO: Implement functional tests to verify this because unit/integration tests can't test this
    static allowedMethods = [jobStatusChange: 'POST']

    def jobStatusChange() {

        def messageType = request.getHeader('x-amz-sns-message-type')

        log.info "x-amz-sns-message-type :: $messageType"
        log.info request.JSON

        render status: VALID_TYPES.contains(messageType) ? 200 : 400
    }

}
