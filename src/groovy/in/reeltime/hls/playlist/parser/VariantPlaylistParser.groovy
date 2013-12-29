package in.reeltime.hls.playlist.parser

import in.reeltime.hls.playlist.StreamAttributes
import in.reeltime.hls.playlist.VariantPlaylist

import static in.reeltime.hls.playlist.util.PlaylistParserUtils.ensureExtendedM3U
import static in.reeltime.hls.playlist.util.PlaylistParserUtils.checkTag
import static in.reeltime.hls.playlist.parser.StreamAttributesParser.parseAttributes

class VariantPlaylistParser {

    static VariantPlaylist parse(Reader reader) {
        ensureExtendedM3U(reader)

        def playlist = new VariantPlaylist()
        String line = reader.readLine()

        while(line != null) {
            if(isStreamInf(line)) {
                def startIndex = line.indexOf(':') + 1
                def text = line.substring(startIndex)
                def attributes = parseAttributes(text)
                def streamName = reader.readLine()
                def args = attributes << [uri: streamName]
                def streamAttributes = new StreamAttributes(args)
                playlist.streams.add(streamAttributes)
            }
            line = reader.readLine()
        }
        return playlist
    }

    private static boolean isStreamInf(String line) {
        checkTag(line, '#EXT-X-STREAM-INF:')
    }
}
