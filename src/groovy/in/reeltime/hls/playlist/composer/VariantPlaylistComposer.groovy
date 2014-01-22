package in.reeltime.hls.playlist.composer

import in.reeltime.hls.playlist.StreamAttributes

class VariantPlaylistComposer {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator")

    static void compose(Collection<StreamAttributes> streams, Writer writer) {

        def builder = new StringBuilder()
                   .append("#EXTM3U")
                   .append(LINE_SEPARATOR)

        streams.each { stream ->
            builder.append("#EXT-X-STREAM-INF:")
                   .append("PROGRAM-ID=${stream.programId},")
                   .append("RESOLUTION=${stream.resolution},")
                   .append("CODECS=\"${stream.codecs}\",")
                   .append("BANDWIDTH=${stream.bandwidth}")
                   .append(LINE_SEPARATOR)
                   .append(stream.uri)
                   .append(LINE_SEPARATOR)
        }

        writer << builder.toString()
    }
}
