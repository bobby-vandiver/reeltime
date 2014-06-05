package in.reeltime.metadata

class StreamMetadataService {

    def ffprobeService

    List<StreamMetadata> extractStreams(File video) {
        def ffprobeResult = ffprobeService.probeVideo(video)
        def streams = ffprobeResult?.streams
        streams.collect { new StreamMetadata(codecName: it.codec_name, duration: it.duration) }
    }
}
