package in.reeltime.video.transcoder

class NotificationController {

    private static final VALID_TYPES = ['SubscriptionConfirmation', 'Notification']

    def jobStatusChange() {

        def messageType = request.getHeader('x-amz-sns-message-type')

        log.info "x-amz-sns-message-type :: $messageType"
        log.info request.JSON

        render status: VALID_TYPES.contains(messageType) ? 200 : 400
    }

}
