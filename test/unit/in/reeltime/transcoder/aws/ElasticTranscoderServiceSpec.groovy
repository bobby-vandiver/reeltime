package in.reeltime.transcoder.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.*
import grails.test.mixin.TestFor
import in.reeltime.aws.AwsService
import in.reeltime.exceptions.TranscoderException
import in.reeltime.playlist.PlaylistAndSegmentStorageService
import in.reeltime.transcoder.TranscoderJobService
import in.reeltime.transcoder.TranscoderService
import in.reeltime.video.Video
import spock.lang.Specification
import spock.lang.Unroll

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

        service.pipelineName = pipeline.name

        service.inputSettings = [aspectRatio: 'auto', frameRate: 'auto', resolution: 'auto',
                                 interlaced: 'auto', container: 'auto']

        service.segmentDuration = '10'
        service.playlistFormat = 'HLSv3'

        service.transcoderJobService = Mock(TranscoderJobService)
        service.awsService = Mock(AwsService)

        service.playlistAndSegmentStorageService = Stub(PlaylistAndSegmentStorageService) {
            getUniquePlaylistPath() >> UUID.randomUUID()
        }
    }

    void "must be an instance of TranscoderService"() {
        expect:
        service instanceof TranscoderService
    }

    void "wrap any thrown exceptions"() {
        when:
        service.transcode(null, null)

        then:
        def e = thrown(TranscoderException)
        e.cause.class == NullPointerException
    }

    @Unroll
    void "submit video to configured pipeline with [#count] media playlists"() {
        given:
        service.presetIds = presets

        and:
        def video = new Video(masterPath: 'bar')
        def outputPath = UUID.randomUUID().toString()

        and:
        def jobId = '123413212351'

        when:
        service.transcode(video, outputPath)

        then:
        1 * service.awsService.createClient(AmazonElasticTranscoder) >> mockElasticTranscoder

        and:
        1 * mockElasticTranscoder.createJob(_) >> { CreateJobRequest request ->

            def expectations = [
                    pipelineId: pipeline.id,
                    outputKeyPrefix: outputPath + '/',
                    mediaPlaylistCount: count,
                    presetIds: presets.collect { name, presetId -> presetId},
                    segmentDuration: '10',
                    inputKey: video.masterPath
            ]

            def validator = new CreateJobRequestValidator(request)
            validator.validate(expectations)

            def job = Stub(Job) { getId() >> jobId }
            return Stub(CreateJobResult) { getJob() >> job }
        }

        and:
        1 * service.transcoderJobService.createJob(video, jobId)

        where:
        count    |   presets
        1        |   [HLS_400K: '1351620000001-200050']
        2        |   [HLS_400K: '1351620000001-200050', HLS_600K: '1351620000001-200040']
        3        |   [HLS_400K: '1351620000001-200050', HLS_600K: '1351620000001-200040', HLS_1M: '1351620000001-200030']
    }

    private static class CreateJobRequestValidator {

        private final CreateJobRequest request

        CreateJobRequestValidator(CreateJobRequest request) {
            this.request = request
        }

        void validate(Map expectations) {
            pipeline(expectations.pipelineId)
            outputKeyPrefix(expectations.outputKeyPrefix)
            variantPlaylist()
            mediaPlaylists(expectations.mediaPlaylistCount, expectations.presetIds, expectations.segmentDuration)
            jobInput(expectations.inputKey)
        }

        private void pipeline(pipelineId) {
            assert request.pipelineId == pipelineId
        }

        private void outputKeyPrefix(path) {
            def outputKeyPrefix = request.outputKeyPrefix
            assert outputKeyPrefix.endsWith('/')
            assert outputKeyPrefix[0..-2].matches(UUID_REGEX)
            assert outputKeyPrefix == path
        }

        private void variantPlaylist() {
            assert request.playlists.size() == 1

            def variantPlaylist = request.playlists[0]
            assert variantPlaylist.format == 'HLSv3'
            assert variantPlaylist.name.matches(UUID_REGEX)
        }

        private void mediaPlaylists(int mediaPlaylistCount, List presetIds, String segmentDuration) {

            def outputKeys = request.playlists[0].outputKeys
            assert outputKeys.size() == mediaPlaylistCount
            outputKeys.each { assert it.matches(UUID_REGEX) }

            request.outputs.each { output ->
                assert outputKeys.contains(output.key)
                assert presetIds.contains(output.presetId)
                assert output.segmentDuration == segmentDuration
                presetIds.remove(output.presetId)
            }

            assert presetIds.isEmpty()
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
