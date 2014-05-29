package in.reeltime.notification

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.transcoder.TranscoderJob
import in.reeltime.video.Video
import org.apache.commons.logging.Log
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.transcoder.TranscoderJobService
import in.reeltime.playlist.PlaylistService

@TestFor(NotificationController)
@Mock([TranscoderJob])
class NotificationControllerSpec extends Specification {

    @Unroll
    void "message is authentic [#authentic]"() {
        given:
        controller.notificationService = Mock(NotificationService)

        def signingCertUrl = 'https://sns.us-east-1.amazonaws.com/SimpleNotificationService-e372f8ca30337fdb084e8ac449342c77.pem'

        def message = /{"SigningCertURL": "${signingCertUrl}"}/
        def parsedMessage = [SigningCertURL: signingCertUrl]

        and:
        request.content = message.bytes

        when:
        def result = controller.beforeInterceptor()

        then:
        result.is(null) == resultIsNull
        result.is(false) == resultIsFalse

        and:
        (response.status == 400) == statusIs400

        and:
        1 * controller.notificationService.verifyMessage(parsedMessage) >> authentic

        where:
        authentic   |   resultIsNull    |   resultIsFalse   |   statusIs400
        true        |   true            |   false           |   false
        false       |   false           |   true            |   true
    }

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
    void "return 400 if SubscriptionConfirmation message is empty for action [#action]"() {
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
        def token = '2336412f37fb687f5d51e6e241d164b051479845a45fd1e10f1287fbc675dba8bb330f79de4343d9bc6e25954e0b9b47c04d6f9d46d7f52460b8f253675f7909d0d801fa1fb7af7aac2400e9491e815b2b506921d04a2a918d70a75f5768b654b6ad6da9bce8c4c98eb6f16857123e51'
        def topicArn = 'arn:aws:sns:us-east-1:166209233708:ets-listener'
        def message = /{"Token": "${token}", "TopicArn": "${topicArn}"}/

        and:
        request.addHeader('x-amz-sns-message-type', 'SubscriptionConfirmation')
        request.content = message.bytes

        when:
        controller."$action"()

        then:
        response.status == 200

        and:
        1 * controller.notificationService.confirmSubscription(topicArn, token)

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

    void "log the elastic transcoder jobId when progressing notification occurs"() {
        given:
        controller.log = Mock(Log)

        and:
        def message = '''{
                        |    "Message": "{\\n  \\"state\\" : \\"PROGRESSING\\",\\n  \\"version\\" : \\"2012-09-25\\",\\n  \\"jobId\\" : \\"1388444889472-t01s28\\",\\n  \\"pipelineId\\" : \\"1388441748515-gvt196\\",\\n  \\"input\\" : {\\n    \\"key\\" : \\"small.mp4\\",\\n    \\"frameRate\\" : \\"auto\\",\\n    \\"resolution\\" : \\"auto\\",\\n    \\"aspectRatio\\" : \\"auto\\",\\n    \\"interlaced\\" : \\"auto\\",\\n    \\"container\\" : \\"auto\\"\\n  },\\n  \\"outputKeyPrefix\\" : \\"hls-small/\\",\\n  \\"outputs\\" : [ {\\n    \\"id\\" : \\"1\\",\\n    \\"presetId\\" : \\"1351620000001-200050\\",\\n    \\"key\\" : \\"hls-small-400k\\",\\n    \\"thumbnailPattern\\" : \\"\\",\\n    \\"rotate\\" : \\"auto\\",\\n    \\"segmentDuration\\" : 10.0,\\n    \\"status\\" : \\"Progressing\\"\\n  } ],\\n  \\"playlists\\" : [ {\\n    \\"name\\" : \\"hls-small-master\\",\\n    \\"format\\" : \\"HLSv3\\",\\n    \\"outputKeys\\" : [ \\"hls-small-400k\\" ],\\n    \\"status\\" : \\"Progressing\\"\\n  } ]\\n}",
                        |    "Subject": "Amazon Elastic Transcoder has scheduled job 1388444889472-t01s28 for transcoding.",
                        |    "Type": "Notification"
                        |}'''.stripMargin()

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        when:
        controller.progressing()

        then:
        1 * controller.log.debug('Elastic Transcoder job [1388444889472-t01s28] is progressing')

        and:
        response.status == 200
    }

    void "load the elastic transcoder job and delegate to services to complete transcoding process"() {
        given:
        controller.transcoderJobService = Mock(TranscoderJobService)
        controller.playlistService = Mock(PlaylistService)

        and:
        def message = '''{
                        |    "Message": "{\\n  \\"state\\" : \\"COMPLETED\\",\\n  \\"version\\" : \\"2012-09-25\\",\\n  \\"jobId\\" : \\"1388444889472-t01s28\\",\\n  \\"pipelineId\\" : \\"1388441748515-gvt196\\",\\n  \\"input\\" : {\\n    \\"key\\" : \\"small.mp4\\",\\n    \\"frameRate\\" : \\"auto\\",\\n    \\"resolution\\" : \\"auto\\",\\n    \\"aspectRatio\\" : \\"auto\\",\\n    \\"interlaced\\" : \\"auto\\",\\n    \\"container\\" : \\"auto\\"\\n  },\\n  \\"outputKeyPrefix\\" : \\"hls-small/\\",\\n  \\"outputs\\" : [ {\\n    \\"id\\" : \\"1\\",\\n    \\"presetId\\" : \\"1351620000001-200050\\",\\n    \\"key\\" : \\"hls-small-400k\\",\\n    \\"thumbnailPattern\\" : \\"\\",\\n    \\"rotate\\" : \\"auto\\",\\n    \\"segmentDuration\\" : 10.0,\\n    \\"status\\" : \\"Complete\\",\\n    \\"duration\\" : 6,\\n    \\"width\\" : 400,\\n    \\"height\\" : 228\\n  } ],\\n  \\"playlists\\" : [ {\\n    \\"name\\" : \\"hls-small-master\\",\\n    \\"format\\" : \\"HLSv3\\",\\n    \\"outputKeys\\" : [ \\"hls-small-400k\\" ],\\n    \\"status\\" : \\"Complete\\"\\n  } ]\\n}",
                        |    "Subject": "Amazon Elastic Transcoder has finished transcoding job 1388444889472-t01s28.",
                        |    "Type": "Notification"
                        |}'''.stripMargin()

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        and:
        def video = new Video()
        def transcoderJob = new TranscoderJob(video: video, jobId: '1388444889472-t01s28').save(validate: false)

        when:
        controller.completed()

        then:
        1 * controller.transcoderJobService.complete(transcoderJob)
        1 * controller.playlistService.addPlaylists(video, 'hls-small/', 'hls-small-master')

        and:
        response.status == 200
    }
}
