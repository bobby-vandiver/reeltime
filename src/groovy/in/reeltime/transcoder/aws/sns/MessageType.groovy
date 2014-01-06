package in.reeltime.transcoder.aws.sns

class MessageType {

    static final MESSAGE_TYPE_HEADER = 'x-amz-sns-message-type'

    static final SUBSCRIPTION_CONFIRMATION = 'SubscriptionConfirmation'
    static final NOTIFICATION = 'Notification'
}
