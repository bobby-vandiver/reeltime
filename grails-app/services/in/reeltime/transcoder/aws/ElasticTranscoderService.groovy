package in.reeltime.transcoder.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput
import com.amazonaws.services.elastictranscoder.model.CreateJobPlaylist
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest
import com.amazonaws.services.elastictranscoder.model.JobInput
import in.reeltime.exceptions.TranscoderException
import in.reeltime.transcoder.TranscoderService
import in.reeltime.video.Video

class ElasticTranscoderService implements TranscoderService {

    def awsService
    def transcoderJobService
    def pathGenerationService

    def pipelineName

    def inputSettings
    def presetIds

    def segmentDuration
    def playlistFormat

    @Override
    void transcode(Video video, String output) {
        try {
            log.debug("Entering ${this.class.simpleName} transcode with video [${video.id}] and output [$output]")
            def ets = awsService.createClient(AmazonElasticTranscoder) as AmazonElasticTranscoder

            def pipeline = getPipeline(ets)

            def input = createJobInput(video.masterPath)
            def outputs = presetIds.collect { name, presetId -> createJobOutput(presetId) }

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
        catch(Exception e) {
            throw new TranscoderException(e)
        }
    }

    private def getPipeline(AmazonElasticTranscoder ets) {
        log.debug("Searching for pipeline [$pipelineName]")
        ets.listPipelines().pipelines.find { it.name == pipelineName}
    }

    private def createJobInput(String path) {
        def settings = inputSettings + [key: path]
        log.debug("Job input settings: [$settings]")
        new JobInput(settings)
    }

    private def createJobOutput(String presetId) {
        def key = pathGenerationService.uniquePlaylistPath
        log.debug("Job output settings -- key [$key] -- presetId [$presetId] -- duration [$segmentDuration]")
        new CreateJobOutput(key: key, presetId: presetId, segmentDuration: segmentDuration)
    }

    private def createJobPlaylist(Collection<String> outputKeys) {
        def name = pathGenerationService.uniquePlaylistPath
        log.debug("Job playlist settings -- name [$name] -- format [$playlistFormat] -- outputKeys [$outputKeys]")
        new CreateJobPlaylist(format: playlistFormat, name: name, outputKeys: outputKeys)
    }
}
