package in.reeltime.hls.playlist.parser

import in.reeltime.hls.playlist.StreamAttributes
import in.reeltime.hls.playlist.VariantPlaylist

import static in.reeltime.hls.playlist.util.PlaylistParserUtils.ensureExtendedM3U
import static in.reeltime.hls.playlist.util.PlaylistParserUtils.getTagAndParams
import static in.reeltime.hls.playlist.parser.StreamAttributesParser.parseAttributes

class VariantPlaylistParser {

    static VariantPlaylist parse(Reader reader) {
        ensureExtendedM3U(reader)

        def playlist = new VariantPlaylist()
        String line = reader.readLine()

        while(line != null) {

            def (tag, params) = getTagAndParams(line)

            switch (tag) {
                case '#EXT-X-STREAM-INF':
                    def uri = reader.readLine()
                    def attributes = parseAttributes(params)
                    def args = attributes << [uri: uri]
                    playlist.streams << new StreamAttributes(args)
            }

            line = reader.readLine()
        }
        return playlist
    }
}
