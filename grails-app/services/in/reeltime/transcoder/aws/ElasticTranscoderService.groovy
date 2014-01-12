package in.reeltime.transcoder.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput
import com.amazonaws.services.elastictranscoder.model.CreateJobPlaylist
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest
import com.amazonaws.services.elastictranscoder.model.JobInput
import in.reeltime.transcoder.TranscoderService
import in.reeltime.video.Video

class ElasticTranscoderService implements TranscoderService {

    def awsService
    def transcoderJobService

    def grailsApplication

    @Override
    void transcode(Video video, String output){

        def ets = awsService.createClient(AmazonElasticTranscoder) as AmazonElasticTranscoder

        def pipeline = getPipeline(ets)
        def input = createJobInput(video.masterPath)

        def presets = grailsApplication.config.transcoder.output.presets
        def outputs = presets.collect { name, presetId -> createJobOutput(presetId) }

        def outputKeys = outputs.collect { it.key }
        def playlist = createJobPlaylist(outputKeys)

        def outputKeyPrefix = output + '/'
        def request = new CreateJobRequest(
                pipelineId: pipeline.id,
                input: input,
                outputKeyPrefix: outputKeyPrefix,
                outputs: outputs,
                playlists: [playlist]
        )

        def result = ets.createJob(request)
        def jobId = result.job.id

        transcoderJobService.createJob(video, jobId)
    }

    private def getPipeline(AmazonElasticTranscoder ets) {
        def name = grailsApplication.config.transcoder.pipeline
        ets.listPipelines().pipelines.find { it.name == name}
    }

    private def createJobInput(String path) {
        def settings = grailsApplication.config.transcoder.input + [key: path]
        new JobInput(settings)
    }

    private def createJobOutput(String presetId) {
        def key = randomUUIDString()
        def duration = grailsApplication.config.transcoder.output.segmentDuration
        new CreateJobOutput(key: key, presetId: presetId, segmentDuration: duration)
    }

    private def createJobPlaylist(Collection<String> outputKeys) {
        def name = randomUUIDString()
        def format = grailsApplication.config.transcoder.output.format
        new CreateJobPlaylist(format: format, name: name, outputKeys: outputKeys)
    }

    private static String randomUUIDString() {
        UUID.randomUUID() as String
    }
}
