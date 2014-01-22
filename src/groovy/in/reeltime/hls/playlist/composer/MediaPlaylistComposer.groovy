package in.reeltime.hls.playlist.composer

import in.reeltime.hls.playlist.MediaPlaylist

class MediaPlaylistComposer {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator")

    static void compose(MediaPlaylist playlist, Writer writer) {

        def builder = new StringBuilder()

                .append("#EXTM3U")
                .append(LINE_SEPARATOR)

                .append("#EXT-X-VERSION:${playlist.version}")
                .append(LINE_SEPARATOR)

                .append("#EXT-X-MEDIA-SEQUENCE:${playlist.mediaSequence}")
                .append(LINE_SEPARATOR)

                .append("#EXT-X-ALLOW-CACHE:${playlist.allowCache ? 'YES' : 'NO'}")
                .append(LINE_SEPARATOR)

                .append("#EXT-X-TARGETDURATION:${playlist.targetDuration}")
                .append(LINE_SEPARATOR)

        playlist.segments.each { segment ->

            builder.append("#EXTINF:${segment.duration},")
                   .append(LINE_SEPARATOR)

                   .append(segment.uri)
                   .append(LINE_SEPARATOR)
        }

        builder.append("#EXT-X-ENDLIST")
               .append(LINE_SEPARATOR)

        writer << builder.toString()
    }
}
