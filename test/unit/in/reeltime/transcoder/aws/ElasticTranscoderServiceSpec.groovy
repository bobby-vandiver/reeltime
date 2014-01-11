package in.reeltime.transcoder.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput
import com.amazonaws.services.elastictranscoder.model.CreateJobPlaylist
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest
import com.amazonaws.services.elastictranscoder.model.CreateJobResult
import com.amazonaws.services.elastictranscoder.model.Job
import com.amazonaws.services.elastictranscoder.model.JobInput
import com.amazonaws.services.elastictranscoder.model.ListPipelinesResult
import com.amazonaws.services.elastictranscoder.model.Pipeline
import grails.test.mixin.TestFor
import in.reeltime.transcoder.TranscoderJobService
import in.reeltime.transcoder.TranscoderService
import in.reeltime.aws.AwsService
import in.reeltime.video.Video
import spock.lang.Specification

@TestFor(ElasticTranscoderService)
class ElasticTranscoderServiceSpec extends Specification {

    private static final UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/

    void "must be an instance of TranscoderService"() {
        expect:
        service instanceof TranscoderService
    }

    void "submit video to configured pipeline"() {
        given:
        def mockEts = Mock(AmazonElasticTranscoder)

        and:
        service.awsService = Mock(AwsService)
        service.transcoderJobService = Mock(TranscoderJobService)

        and:
        def pipeline = new Pipeline(name: 'foo', id: '132afc')
        grailsApplication.config.transcoder.pipeline = 'foo'

        and:
        grailsApplication.config.transcoder.input.aspectRatio = 'auto'
        grailsApplication.config.transcoder.input.frameRate = 'auto'
        grailsApplication.config.transcoder.input.resolution = 'auto'
        grailsApplication.config.transcoder.input.interlaced = 'auto'
        grailsApplication.config.transcoder.input.container = 'auto'

        and:
        grailsApplication.config.transcoder.output.segmentDuration = '10'
        grailsApplication.config.transcoder.output.format = 'HLSv3'
        grailsApplication.config.transcoder.output.presets = ['400k': '1351620000001-200050']

        and:
        service.grailsApplication = grailsApplication

        and:
        def video = new Video(masterPath: 'bar')
        def jobId = '123413212351'

        when:
        service.transcode(video)

        then:
        1 * service.awsService.createClient(AmazonElasticTranscoder) >> mockEts

        and:
        1 * mockEts.listPipelines() >> Stub(ListPipelinesResult) {
            getPipelines() >> [pipeline]
        }

        and:
        1 * mockEts.createJob(_) >> { CreateJobRequest request ->

            assert request.pipelineId == pipeline.id

            def outputKeyPrefix = request.outputKeyPrefix
            assert outputKeyPrefix.endsWith('/')
            assert outputKeyPrefix[0..-2].matches(UUID_REGEX)

            def outputs = request.outputs
            assert outputs.size() == 1
            validateJobOutput(request.outputs[0])

            def playlists = request.playlists
            assert playlists.size() == 1
            validatePlaylist(playlists[0])

            validateJobInput(request.input, video.masterPath)

            def job = Stub(Job) { getId() >> jobId }
            return Stub(CreateJobResult) { getJob() >> job }
        }

        and:
        1 * service.transcoderJobService.createJob(video, jobId)
    }

    private void validatePlaylist(CreateJobPlaylist playlist) {
        assert playlist.format == 'HLSv3'
        assert playlist.name.matches(UUID_REGEX)

        assert playlist.outputKeys.size() == 1
        assert playlist.outputKeys[0].matches(UUID_REGEX)
    }

    private void validateJobOutput(CreateJobOutput output) {
        assert output.key.matches(UUID_REGEX)
        assert output.presetId == '1351620000001-200050'
        assert output.segmentDuration == '10'
    }

    private void validateJobInput(JobInput input, String expectedKey) {
        assert input.key == expectedKey
        assert input.aspectRatio == 'auto'
        assert input.frameRate == 'auto'
        assert input.resolution == 'auto'
        assert input.interlaced == 'auto'
        assert input.container == 'auto'
    }
}
