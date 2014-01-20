package in.reeltime.playlist

import in.reeltime.hls.playlist.parser.VariantPlaylistParser
import in.reeltime.hls.playlist.parser.MediaPlaylistParser

class PlaylistParserService {

    def outputStorageService

    def parseVariantPlaylist(String path) {
        parse(path) { reader -> VariantPlaylistParser.parse(reader) }
    }

    def parseMediaPlaylist(String path) {
        parse(path) { reader -> MediaPlaylistParser.parse(reader) }
    }

    private def parse(String path, Closure parser) {
        log.debug("Parsing playlist at path [$path]")
        def playlistStream = outputStorageService.load(path) as InputStream
        playlistStream.withReader { reader -> parser(reader) }
    }
}
