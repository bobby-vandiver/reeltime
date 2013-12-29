package in.reeltime.hls.playlist.util

class PlaylistParserUtils {

    private static final int INDEX_NOT_FOUND = -1

    static void ensureExtendedM3U(Reader reader) {
        if(!isExtendedM3U(reader))
            throw new IllegalArgumentException('First line of playlist must be #EXTM3U')
    }

    private static boolean isExtendedM3U(Reader reader) {
        reader.readLine() == '#EXTM3U'
    }

    static List getTagAndParams(String line) {
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
}
