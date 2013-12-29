package in.reeltime.hls.playlist.parser

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment

import static in.reeltime.hls.playlist.util.PlaylistParserUtils.ensureExtendedM3U
import static in.reeltime.hls.playlist.util.PlaylistParserUtils.getTagAndParams

class MediaPlaylistParser {

    static MediaPlaylist parse(Reader reader) {
        ensureExtendedM3U(reader)

        MediaPlaylist playlist = new MediaPlaylist()
        String line = reader.readLine()

        while(line != null) {
            def (tag, params) = getTagAndParams(line)

            switch(tag) {
                case '#EXT-X-TARGETDURATION':
                    playlist.targetDuration = params as int
                    break

                case '#EXT-X-MEDIA-SEQUENCE':
                    playlist.mediaSequence = params as int
                    break

                case '#EXT-X-VERSION':
                    playlist.version = params as int
                    break

                case '#EXT-X-ALLOW-CACHE':
                    throwIfAllowCacheIsInvalid(params)
                    playlist.allowCache = (params == 'YES')
                    break

                case '#EXTINF':
                    def uri = reader.readLine()
                    def duration = getSegmentDuration(line)
                    playlist.segments << new MediaSegment(uri: uri, duration: duration)
                    break
            }

            line = reader.readLine()
        }

        return playlist
    }

    private static String getSegmentDuration(String line) {
        def segmentInfo = getTagAndParams(line)[1]
        def endIndex = segmentInfo.indexOf(',')
        def duration = segmentInfo.substring(0, endIndex)
        return duration
    }

    private static void throwIfAllowCacheIsInvalid(String allowed) {
        if(allowed != 'YES' && allowed != 'NO')
            throw new IllegalArgumentException('#EXT-X-ALLOW-CACHE must be YES or NO')
    }
}
