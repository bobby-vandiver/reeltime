package in.reeltime.hls.playlist.slurper

import static in.reeltime.hls.playlist.util.PlaylistSlurperUtils.ensureExtendedM3U
import static in.reeltime.hls.playlist.util.PlaylistSlurperUtils.checkTag

class VariantPlaylistSlurper {

    private static final String ATTRIBUTE_SEPARATOR = ','

    private static final String CODEC_FORMAT_START = '"'
    private static final String CODEC_FORMAT_SEPARATOR = '",'

    Map parse(Reader reader) {
        ensureExtendedM3U(reader)

        def playlist = [:]
        String line = reader.readLine()

        while(line != null) {
            if(isStreamInf(line)) {
                def startIndex = line.indexOf(':') + 1
                def text = line.substring(startIndex)
                def attributes = parseAttributes(text)
                def streamName = reader.readLine()
                playlist << [(streamName) : attributes]
            }
            line = reader.readLine()
        }
        return playlist
    }

    private static boolean isStreamInf(String line) {
        checkTag(line, '#EXT-X-STREAM-INF:')
    }

    private static Map parseAttributes(String text) {
        def attributes = [:]
        def idx = 0

        while(idx < text.length()) {

            String key = nextKey(idx, text)
            idx += key.length() + 1

            def value = nextValue(idx, text)
            idx += value.length() + 1

            attributes += convertAttributeToMap(key, value)
        }

        return attributes
    }

    private static String nextKey(int keyStart, String text) {
        def keyEnd = text.indexOf('=', keyStart)
        return text.substring(keyStart, keyEnd)
    }

    private static String nextValue(int valueStart, String text) {
        def valueEnd = findEndOfValue(valueStart, text)
        return isLastValue(valueEnd) ? text.substring(valueStart) : text.substring(valueStart, valueEnd)
    }

    private static findEndOfValue(int startIndex, String text) {
        String delimiter = getValueDelimiter(text, startIndex)
        def endIndex = text.indexOf(delimiter, startIndex)

        if(!isLastValue(endIndex) && isCodecFormat(delimiter)) {
            endIndex++
        }

        return endIndex
    }

    private static String getValueDelimiter(String text, int startIndex) {
        text[startIndex] == CODEC_FORMAT_START ? CODEC_FORMAT_SEPARATOR : ATTRIBUTE_SEPARATOR
    }

    private static boolean isLastValue(int idx) {
        return idx == -1
    }

    private static boolean isCodecFormat(String delimiter) {
        return delimiter == CODEC_FORMAT_SEPARATOR
    }

    private static Map convertAttributeToMap(String key, String value) {
        def attribute = [:]

        switch (key) {
            case 'BANDWIDTH':
                attribute = [bandwidth: value as int]
                break

            case 'PROGRAM-ID':
                attribute = [programId: value as int]
                break

            case 'CODECS':
                def formats = value[1..-2]
                attribute = [codecs: formats]
                break

            case 'RESOLUTION':
                attribute = [resolution: value]
                break
        }

        return attribute
    }
}
