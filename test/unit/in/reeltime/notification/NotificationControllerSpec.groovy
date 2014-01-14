package in.reeltime.notification

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.transcoder.TranscoderJob
import in.reeltime.video.Video
import org.apache.commons.logging.Log
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.transcoder.TranscoderJobService
import in.reeltime.video.playlist.PlaylistService

@TestFor(NotificationController)
@Mock([TranscoderJob])
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
        1 * controller.playlistService.addPlaylists(video, 'hls-small-master')

        and:
        response.status == 200
    }
}
