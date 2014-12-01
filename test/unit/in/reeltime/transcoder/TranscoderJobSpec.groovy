package in.reeltime.transcoder

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.video.Video
import spock.lang.Specification
import spock.lang.Unroll

import static in.reeltime.transcoder.TranscoderJobStatus.*

@TestFor(TranscoderJob)
@Mock(Video)
class TranscoderJobSpec extends Specification {

    void "job must be associated with a video when submitted"() {
        given:
        def video = new Video()
        def jobId = '1388444889472-t01s28'

        when:
        def job = new TranscoderJob(video: video, jobId: jobId)

        then:
        job.validate()

        and:
        job.video == video
        job.jobId == jobId
        job.status == Submitted
    }

    void "video is required"() {
        when:
        def job = new TranscoderJob(video: null)

        then:
        !job.validate(['video'])
    }

    void "video must be unique"() {
        given:
        def video = new Video()

        and:
        def existingJob = new TranscoderJob(video: video)
        mockForConstraintsTests(TranscoderJob, [existingJob])

        when:
        def job = new TranscoderJob(video: video)

        then:
        !job.validate(['video'])
    }

    @Unroll
    void "jobId [#jobId] is valid [#valid]"() {
        when:
        def job = new TranscoderJob(jobId: jobId)

        then:
        job.validate(['jobId']) == valid

        where:
        jobId                   |   valid
        '1388444889472-t01s28'  |   true
        '1234567890123-ABCDEF'  |   true
        '1388444889472-t01s2'   |   false
        '1388444889472-t01s281' |   false
        '1388444889472-t@1s28'  |   false
        '138844488947-t01s28'   |   false
        '11388444889472-t01s28' |   false
        '138844488947A-t01s28'  |   false
        'A1234567890123-ABCDEF' |   false
        null                    |   false
        ''                      |   false
    }

    @Unroll
    void "status [#status] is valid [#valid]"() {
        when:
        def job = new TranscoderJob(status: status)

        then:
        job.validate(['status']) == valid

        where:
        status                      |   valid
        Submitted                   |   true
        Progressing                 |   true
        Complete                    |   true
        Canceled                    |   true
        TranscoderJobStatus.Error   |   true
        'Foo'                       |   false
        null                        |   false
        ''                          |   false
    }
}
