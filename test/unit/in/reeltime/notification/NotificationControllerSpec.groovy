package in.reeltime.notification

import grails.test.mixin.TestFor
import in.reeltime.exceptions.TranscoderJobNotFoundException
import in.reeltime.video.VideoCreationService
import org.apache.commons.logging.Log
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(NotificationController)
class NotificationControllerSpec extends Specification {

    NotificationService notificationService
    VideoCreationService videoCreationService

    void setup() {
        notificationService = Mock(NotificationService)
        videoCreationService = Mock(VideoCreationService)

        controller.notificationService = notificationService
        controller.videoCreationService = videoCreationService
    }

    @Unroll
    void "message is authentic [#authentic]"() {
        given:
        def signingCertUrl = 'https://sns.us-east-1.amazonaws.com/SimpleNotificationService-e372f8ca30337fdb084e8ac449342c77.pem'
        def token = '2336412f37fb687f5d51e6e241d164b051479845a45fd1e10f1287fbc675dba8bb330f79de4343d9bc6e25954e0b9b47c04d6f9d46d7f52460b8f253675f7909d0d801fa1fb7af7aac2400e9491e815b2b506921d04a2a918d70a75f5768b654b6ad6da9bce8c4c98eb6f16857123e51'
        def topicArn = 'arn:aws:sns:us-east-1:166209233708:ets-listener'

        def message = /{"SigningCertURL": "${signingCertUrl}", "Token": "${token}", "TopicArn": "${topicArn}"}/
        def parsedMessage = [SigningCertURL: signingCertUrl, Token: token, TopicArn: topicArn]

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
        1 * notificationService.verifyMessage(parsedMessage) >> authentic

        where:
        authentic   |   resultIsNull    |   resultIsFalse   |   statusIs400
        true        |   true            |   false           |   false
        false       |   false           |   true            |   true
    }

    void "return 400 if AWS SNS message type header is not in request"() {
        when:
        controller.handleMessage()

        then:
        response.status == 400
    }

    @Unroll
    void "return 400 for invalid message type [#type]"() {
        given:
        request.addHeader('x-amz-sns-message-type', type)
        request.content = '{}'.bytes

        when:
        controller.handleMessage()

        then:
        response.status == 400

        where:
        type << ['UnsubscribeConfirmation', 'BadConfirmation', 'Notifications', '']
    }

    void "return 400 if SubscriptionConfirmation message is empty"() {
        given:
        request.addHeader('x-amz-sns-message-type', 'SubscriptionConfirmation')
        request.content = '{}'.bytes

        when:
        controller.handleMessage()

        then:
        response.status == 400

        and:
        0 * notificationService.confirmSubscription(*_)
    }

    void "return 200 after confirming subscription"() {
        given:
        def token = '2336412f37fb687f5d51e6e241d164b051479845a45fd1e10f1287fbc675dba8bb330f79de4343d9bc6e25954e0b9b47c04d6f9d46d7f52460b8f253675f7909d0d801fa1fb7af7aac2400e9491e815b2b506921d04a2a918d70a75f5768b654b6ad6da9bce8c4c98eb6f16857123e51'
        def topicArn = 'arn:aws:sns:us-east-1:166209233708:ets-listener'
        def message = /{"Token": "${token}", "TopicArn": "${topicArn}"}/

        and:
        request.addHeader('x-amz-sns-message-type', 'SubscriptionConfirmation')
        request.content = message.bytes

        when:
        controller.handleMessage()

        then:
        response.status == 200

        and:
        1 * notificationService.confirmSubscription(topicArn, token)
    }

    @Unroll
    void "log entire message when [#state] notification occurs"() {
        given:
        controller.log = Mock(Log)

        and:
        def message = """{
                        |    "Message": "{\\n  \\"state\\" : \\"$state\\",\\n  \\"version\\" : \\"2012-09-25\\",\\n  \\"jobId\\" : \\"1388444889472-t01s28\\",\\n  \\"pipelineId\\" : \\"1388441748515-gvt196\\",\\n  \\"input\\" : {\\n    \\"key\\" : \\"small.mp4\\",\\n    \\"frameRate\\" : \\"auto\\",\\n    \\"resolution\\" : \\"auto\\",\\n    \\"aspectRatio\\" : \\"auto\\",\\n    \\"interlaced\\" : \\"auto\\",\\n    \\"container\\" : \\"auto\\"\\n  },\\n  \\"outputKeyPrefix\\" : \\"hls-small/\\",\\n  \\"outputs\\" : [ {\\n    \\"id\\" : \\"1\\",\\n    \\"presetId\\" : \\"1351620000001-200050\\",\\n    \\"key\\" : \\"hls-small-400k\\",\\n    \\"thumbnailPattern\\" : \\"\\",\\n    \\"rotate\\" : \\"auto\\",\\n    \\"segmentDuration\\" : 10.0,\\n    \\"status\\" : \\"Progressing\\"\\n  } ],\\n  \\"playlists\\" : [ {\\n    \\"name\\" : \\"hls-small-master\\",\\n    \\"format\\" : \\"HLSv3\\",\\n    \\"outputKeys\\" : [ \\"hls-small-400k\\" ],\\n    \\"status\\" : \\"Progressing\\"\\n  } ]\\n}",
                        |    "Type": "Notification"
                        |}""".stripMargin()

        and:
        def expectedLogMessage = "Received ${state.toLowerCase()} notification for job [1388444889472-t01s28]: $message"

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        when:
        controller.handleMessage()

        then:
        1 * controller.log."$method"(expectedLogMessage)

        and:
        response.status == 200

        where:
        state       |   method
        'WARNING'   |   'warn'
        'ERROR'     |   'error'
    }

    void "log message for message with unknown state"() {
        given:
        controller.log = Mock(Log)

        and:
        def message = """{
                        |    "Message": "{\\n  \\"state\\" : \\"UNKNOWN\\",\\n  \\"version\\" : \\"2012-09-25\\",\\n  \\"jobId\\" : \\"1388444889472-t01s28\\",\\n  \\"pipelineId\\" : \\"1388441748515-gvt196\\",\\n  \\"input\\" : {\\n    \\"key\\" : \\"small.mp4\\",\\n    \\"frameRate\\" : \\"auto\\",\\n    \\"resolution\\" : \\"auto\\",\\n    \\"aspectRatio\\" : \\"auto\\",\\n    \\"interlaced\\" : \\"auto\\",\\n    \\"container\\" : \\"auto\\"\\n  },\\n  \\"outputKeyPrefix\\" : \\"hls-small/\\",\\n  \\"outputs\\" : [ {\\n    \\"id\\" : \\"1\\",\\n    \\"presetId\\" : \\"1351620000001-200050\\",\\n    \\"key\\" : \\"hls-small-400k\\",\\n    \\"thumbnailPattern\\" : \\"\\",\\n    \\"rotate\\" : \\"auto\\",\\n    \\"segmentDuration\\" : 10.0,\\n    \\"status\\" : \\"Progressing\\"\\n  } ],\\n  \\"playlists\\" : [ {\\n    \\"name\\" : \\"hls-small-master\\",\\n    \\"format\\" : \\"HLSv3\\",\\n    \\"outputKeys\\" : [ \\"hls-small-400k\\" ],\\n    \\"status\\" : \\"Progressing\\"\\n  } ]\\n}",
                        |    "Type": "Notification"
                        |}""".stripMargin()

        and:
        def expectedLogMessage = "Received unknown state [UNKNOWN] for job [1388444889472-t01s28]: $message"

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        when:
        controller.handleMessage()

        then:
        1 * controller.log.warn(expectedLogMessage)

        and:
        response.status == 200
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
        controller.handleMessage()

        then:
        1 * controller.log.debug('Elastic Transcoder job [1388444889472-t01s28] is progressing')

        and:
        response.status == 200
    }

    void "load the elastic transcoder job and delegate to services to complete transcoding process"() {
        given:
        def message = '''{
                        |    "Message": "{\\n  \\"state\\" : \\"COMPLETED\\",\\n  \\"version\\" : \\"2012-09-25\\",\\n  \\"jobId\\" : \\"1388444889472-t01s28\\",\\n  \\"pipelineId\\" : \\"1388441748515-gvt196\\",\\n  \\"input\\" : {\\n    \\"key\\" : \\"small.mp4\\",\\n    \\"frameRate\\" : \\"auto\\",\\n    \\"resolution\\" : \\"auto\\",\\n    \\"aspectRatio\\" : \\"auto\\",\\n    \\"interlaced\\" : \\"auto\\",\\n    \\"container\\" : \\"auto\\"\\n  },\\n  \\"outputKeyPrefix\\" : \\"hls-small/\\",\\n  \\"outputs\\" : [ {\\n    \\"id\\" : \\"1\\",\\n    \\"presetId\\" : \\"1351620000001-200050\\",\\n    \\"key\\" : \\"hls-small-400k\\",\\n    \\"thumbnailPattern\\" : \\"\\",\\n    \\"rotate\\" : \\"auto\\",\\n    \\"segmentDuration\\" : 10.0,\\n    \\"status\\" : \\"Complete\\",\\n    \\"duration\\" : 6,\\n    \\"width\\" : 400,\\n    \\"height\\" : 228\\n  } ],\\n  \\"playlists\\" : [ {\\n    \\"name\\" : \\"hls-small-master\\",\\n    \\"format\\" : \\"HLSv3\\",\\n    \\"outputKeys\\" : [ \\"hls-small-400k\\" ],\\n    \\"status\\" : \\"Complete\\"\\n  } ]\\n}",
                        |    "Subject": "Amazon Elastic Transcoder has finished transcoding job 1388444889472-t01s28.",
                        |    "Type": "Notification"
                        |}'''.stripMargin()

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        when:
        controller.handleMessage()

        then:
        1 * videoCreationService.completeVideoCreation('1388444889472-t01s28', 'hls-small/', 'hls-small-master')

        and:
        response.status == 200
    }

    void "elastic transcoder job cannot be found"() {
        given:
        controller.log = Mock(Log)

        and:
        def message = '''{
                        |    "Message": "{\\n  \\"state\\" : \\"COMPLETED\\",\\n  \\"version\\" : \\"2012-09-25\\",\\n  \\"jobId\\" : \\"1388444889472-t01s28\\",\\n  \\"pipelineId\\" : \\"1388441748515-gvt196\\",\\n  \\"input\\" : {\\n    \\"key\\" : \\"small.mp4\\",\\n    \\"frameRate\\" : \\"auto\\",\\n    \\"resolution\\" : \\"auto\\",\\n    \\"aspectRatio\\" : \\"auto\\",\\n    \\"interlaced\\" : \\"auto\\",\\n    \\"container\\" : \\"auto\\"\\n  },\\n  \\"outputKeyPrefix\\" : \\"hls-small/\\",\\n  \\"outputs\\" : [ {\\n    \\"id\\" : \\"1\\",\\n    \\"presetId\\" : \\"1351620000001-200050\\",\\n    \\"key\\" : \\"hls-small-400k\\",\\n    \\"thumbnailPattern\\" : \\"\\",\\n    \\"rotate\\" : \\"auto\\",\\n    \\"segmentDuration\\" : 10.0,\\n    \\"status\\" : \\"Complete\\",\\n    \\"duration\\" : 6,\\n    \\"width\\" : 400,\\n    \\"height\\" : 228\\n  } ],\\n  \\"playlists\\" : [ {\\n    \\"name\\" : \\"hls-small-master\\",\\n    \\"format\\" : \\"HLSv3\\",\\n    \\"outputKeys\\" : [ \\"hls-small-400k\\" ],\\n    \\"status\\" : \\"Complete\\"\\n  } ]\\n}",
                        |    "Subject": "Amazon Elastic Transcoder has finished transcoding job 1388444889472-t01s28.",
                        |    "Type": "Notification"
                        |}'''.stripMargin()

        and:
        request.addHeader('x-amz-sns-message-type', 'Notification')
        request.content = message.bytes

        when:
        controller.handleMessage()

        then:
        1 * videoCreationService.completeVideoCreation('1388444889472-t01s28', 'hls-small/', 'hls-small-master') >> { throw new TranscoderJobNotFoundException('TEST') }
        1 * controller.log.warn('Could not find transcoder job [1388444889472-t01s28] -- assuming it was already removed')

        and:
        response.status == 200
    }
}
