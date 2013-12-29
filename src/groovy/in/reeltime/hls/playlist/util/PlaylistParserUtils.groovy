package in.reeltime.hls.playlist.util

class PlaylistParserUtils {

    static void ensureExtendedM3U(Reader reader) {
        if(!isExtendedM3U(reader))
            throw new IllegalArgumentException('First line of playlist must be #EXTM3U')
    }

    private static boolean isExtendedM3U(Reader reader) {
        reader.readLine() == '#EXTM3U'
    }

    static boolean checkTag(String line, String tag) {
        line.startsWith(tag)
    }
}
