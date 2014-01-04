package in.reeltime.video.transcoder

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(NotificationController)
class NotificationControllerSpec extends Specification {

    @Unroll
    void "return 400 if AWS SNS message type header is not in request for [#action]"() {
        when:
        controller."$action"()

        then:
        response.status == 400

        where:
        action << ['completed', 'progressing', 'warning', 'error']
    }

    @Unroll
    void "return [#statusCode] for message type [#type]"() {
        expect:
        assertActionReturnsStatusCodeForMessageType('completed', statusCode, type)
        assertActionReturnsStatusCodeForMessageType('progressing', statusCode, type)
        assertActionReturnsStatusCodeForMessageType('warning', statusCode, type)
        assertActionReturnsStatusCodeForMessageType('error', statusCode, type)

        where:
        statusCode      |   type
        200             |   'SubscriptionConfirmation'
        200             |   'Notification'
        400             |   'UnsubscribeConfirmation'
        400             |   'BadConfirmation'
        400             |   'Notifications'
        400             |   ''
    }

    private void assertActionReturnsStatusCodeForMessageType(action, statusCode, messageType) {
        response.reset()
        request.addHeader('x-amz-sns-message-type', messageType)

        controller."$action"()
        assert response.status == statusCode
    }
}
