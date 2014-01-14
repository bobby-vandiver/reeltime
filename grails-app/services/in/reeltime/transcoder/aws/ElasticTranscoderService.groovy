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
    def pathGenerationService

    def grailsApplication

    @Override
    void transcode(Video video, String output){

        log.debug("Entering ${this.class.simpleName} transcode with video [${video.id}] and output [$output]")
        def ets = awsService.createClient(AmazonElasticTranscoder) as AmazonElasticTranscoder

        def pipeline = getPipeline(ets)
        def input = createJobInput(video.masterPath)

        def presets = grailsApplication.config.reeltime.transcoder.output.presets
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

        log.info("Submitting job to Elastic Transcoder for video [${video.id}] to be output to bucket [$output]")
        def result = ets.createJob(request)
        def jobId = result.job.id

        transcoderJobService.createJob(video, jobId)
    }

    private def getPipeline(AmazonElasticTranscoder ets) {
        def name = grailsApplication.config.reeltime.transcoder.pipeline
        log.debug("Searching for pipeline [$name]")
        ets.listPipelines().pipelines.find { it.name == name}
    }

    private def createJobInput(String path) {
        def settings = grailsApplication.config.reeltime.transcoder.input + [key: path]
        log.debug("Job input settings: [$settings]")
        new JobInput(settings)
    }

    private def createJobOutput(String presetId) {
        def key = pathGenerationService.uniqueOutputPath
        def duration = grailsApplication.config.reeltime.transcoder.output.segmentDuration
        log.debug("Job output settings -- key [$key] -- presetId [$presetId] -- duration [$duration]")
        new CreateJobOutput(key: key, presetId: presetId, segmentDuration: duration)
    }

    private def createJobPlaylist(Collection<String> outputKeys) {
        def name = pathGenerationService.uniqueOutputPath
        def format = grailsApplication.config.reeltime.transcoder.output.format
        log.debug("Job playlist settings -- name [$name] -- format [$format] -- outputKeys [$outputKeys]")
        new CreateJobPlaylist(format: format, name: name, outputKeys: outputKeys)
    }
}
