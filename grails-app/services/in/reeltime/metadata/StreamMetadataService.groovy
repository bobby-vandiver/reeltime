package in.reeltime.metadata

class StreamMetadataService {

    def ffprobeService

    List<StreamMetadata> extractStreams(File video) {

        def ffprobeResult = ffprobeService.probeVideo(video)
        def streams = ffprobeResult.streams

        def list = []
        streams.each { stream ->
            list << new StreamMetadata(codecName: stream.codec_name, duration: stream.duration)
        }
        return list
    }
}
