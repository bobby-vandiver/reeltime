package in.reeltime.hls.playlist.parser

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment

import static in.reeltime.hls.playlist.util.PlaylistParserUtils.ensureExtendedM3U

class MediaPlaylistParser {

    private static final int INDEX_NOT_FOUND = -1

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

    private static List getTagAndParams(String line) {
        def endOfTag = line.indexOf(':')

        def tag
        def params

        if(endOfTag == INDEX_NOT_FOUND) {
            tag = line.substring(0)
            params = null
        }
        else {
            tag = line.substring(0, endOfTag)
            def startOfParams = endOfTag + 1
            params = line.substring(startOfParams)
        }

        return [tag, params]
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
