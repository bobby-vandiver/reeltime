package in.reeltime.transcoder.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput
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

    private Pipeline pipeline
    private AmazonElasticTranscoder mockElasticTranscoder

    void setup() {
        pipeline = new Pipeline(name: 'testPipeline', id: '132afc')

        mockElasticTranscoder = Mock(AmazonElasticTranscoder) {
            listPipelines() >> Stub(ListPipelinesResult) {
                getPipelines() >> [pipeline]
            }
        }

        grailsApplication.config.transcoder.pipeline = pipeline.name

        grailsApplication.config.transcoder.input.aspectRatio = 'auto'
        grailsApplication.config.transcoder.input.frameRate = 'auto'
        grailsApplication.config.transcoder.input.resolution = 'auto'
        grailsApplication.config.transcoder.input.interlaced = 'auto'
        grailsApplication.config.transcoder.input.container = 'auto'

        grailsApplication.config.transcoder.output.segmentDuration = '10'
        grailsApplication.config.transcoder.output.format = 'HLSv3'
        grailsApplication.config.transcoder.output.presets = ['400k': '1351620000001-200050']

        service.grailsApplication = grailsApplication

        service.transcoderJobService = Mock(TranscoderJobService)
        service.awsService = Mock(AwsService)
    }

    void "must be an instance of TranscoderService"() {
        expect:
        service instanceof TranscoderService
    }

    void "submit video to configured pipeline"() {
        given:
        def video = new Video(masterPath: 'bar')
        def jobId = '123413212351'

        when:
        service.transcode(video)

        then:
        1 * service.awsService.createClient(AmazonElasticTranscoder) >> mockElasticTranscoder

        and:
        1 * mockElasticTranscoder.createJob(_) >> { CreateJobRequest request ->

            def validator = new CreateJobRequestValidator(request)
            validator.validate([pipelineId: pipeline.id, mediaPlaylistCount: 1, inputKey: video.masterPath])

            def job = Stub(Job) { getId() >> jobId }
            return Stub(CreateJobResult) { getJob() >> job }
        }

        and:
        1 * service.transcoderJobService.createJob(video, jobId)
    }

    private static class CreateJobRequestValidator {

        private final CreateJobRequest request

        CreateJobRequestValidator(CreateJobRequest request) {
            this.request = request
        }

        void validate(Map expectations) {
            pipeline(expectations.pipelineId)
            outputKeyPrefix()
            variantPlaylist()
            mediaPlaylists(expectations.mediaPlaylistCount)
            jobInput(expectations.inputKey)
        }

        private void pipeline(pipelineId) {
            assert request.pipelineId == pipelineId
        }

        private void outputKeyPrefix() {
            def outputKeyPrefix = request.outputKeyPrefix
            assert outputKeyPrefix.endsWith('/')
            assert outputKeyPrefix[0..-2].matches(UUID_REGEX)
        }

        private void variantPlaylist() {
            assert request.playlists.size() == 1

            def variantPlaylist = request.playlists[0]
            assert variantPlaylist.format == 'HLSv3'
            assert variantPlaylist.name.matches(UUID_REGEX)
        }

        private void mediaPlaylists(int mediaPlaylistCount) {

            def outputKeys = request.playlists[0].outputKeys
            assert outputKeys.size() == mediaPlaylistCount
            outputKeys.each { assert it.matches(UUID_REGEX) }

            request.outputs.each { output ->
                assert outputKeys.contains(output.key)
                assert output.presetId == '1351620000001-200050'
                assert output.segmentDuration == '10'
            }
        }

        private void jobInput(String expectedKey) {
            def input = request.input
            assert input.key == expectedKey
            assert input.aspectRatio == 'auto'
            assert input.frameRate == 'auto'
            assert input.resolution == 'auto'
            assert input.interlaced == 'auto'
            assert input.container == 'auto'
        }
    }
}
