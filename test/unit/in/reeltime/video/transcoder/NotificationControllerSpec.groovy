package in.reeltime.video.transcoder

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(NotificationController)
class NotificationControllerSpec extends Specification {

    void "return 400 if AWS SNS message type header is not in request"() {
        when:
        controller.jobStatusChange()

        then:
        controller.response.status == 400
    }

    @Unroll
    void "return [#statusCode] for message type [#type]"() {
        given:
        controller.request.addHeader('x-amz-sns-message-type', type)

        when:
        controller.jobStatusChange()

        then:
        controller.response.status == statusCode

        where:
        statusCode      |   type
        200             |   'SubscriptionConfirmation'
        200             |   'Notification'
        400             |   'UnsubscribeConfirmation'
        400             |   'BadConfirmation'
        400             |   'Notifications'
        400             |   ''
    }
}
