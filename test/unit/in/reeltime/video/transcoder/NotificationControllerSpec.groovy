package in.reeltime.video.transcoder

import grails.converters.JSON
import grails.test.mixin.TestFor
import org.apache.commons.logging.Log
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
    void "return 400 for invalid message type [#type]"() {
        expect:
        assertActionReturns400ForInvalidMessageType('completed', type)
        assertActionReturns400ForInvalidMessageType('progressing', type)
        assertActionReturns400ForInvalidMessageType('warning', type)
        assertActionReturns400ForInvalidMessageType('error', type)

        where:
        type << ['UnsubscribeConfirmation', 'BadConfirmation', 'Notifications', '']
    }

    private void assertActionReturns400ForInvalidMessageType(action, messageType) {
        response.reset()
        request.addHeader('x-amz-sns-message-type', messageType)

        controller."$action"()
        assert response.status == 400
    }

    @Unroll
    void "return 400 if SubscriptionConfirmation message does not contain SubscribeURL for action [#action]"() {
        given:
        controller.notificationService = Mock(NotificationService)

        and:
        request.addHeader('x-amz-sns-message-type', 'SubscriptionConfirmation')
        request.content = '{}'.bytes

        when:
        controller."$action"()

        then:
        response.status == 400

        and:
        0 * controller.notificationService.confirmSubscription(_)

        where:
        action << ['completed', 'progressing', 'warning', 'error']
    }

    @Unroll
    void "return 200 after confirming subscription for action [#action]"() {
        given:
        controller.notificationService = Mock(NotificationService)

        and:
        def url = 'https://sns.us-east-1.amazonaws.com/?Action=ConfirmSubscription'
        def message = /{"SubscribeURL": "${url}"}/

        and:
        request.addHeader('x-amz-sns-message-type', 'SubscriptionConfirmation')
        request.content = message.bytes

        when:
        controller."$action"()

        then:
        response.status == 200

        and:
        1 * controller.notificationService.confirmSubscription(url)

        where:
        action << ['completed', 'progressing', 'warning', 'error']
    }

    @Unroll
    void "log entire message when [#action] notification occurs"() {
        given:
        controller.log = Mock(Log)

        and:
        def message = '{foo: bar}'

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        when:
        controller."$action"()

        then:
        1 * controller.log."$method"(message)

        and:
        response.status == 200

        where:
        action      |   method
        'warning'   |   'warn'
        'error'     |   'error'
    }
}
