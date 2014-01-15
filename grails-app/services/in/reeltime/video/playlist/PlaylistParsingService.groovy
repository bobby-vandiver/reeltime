package in.reeltime.video.playlist

import in.reeltime.hls.playlist.parser.VariantPlaylistParser
import in.reeltime.hls.playlist.parser.MediaPlaylistParser

class PlaylistParsingService {

    def outputStorageService

    def parseVariantPlaylist(String path) {
        parse(path) { reader -> VariantPlaylistParser.parse(reader) }
    }

    def parseMediaPlaylist(String path) {
        parse(path) { reader -> MediaPlaylistParser.parse(reader) }
    }

    private def parse(String path, Closure parser) {
        def playlistStream = outputStorageService.load(path) as InputStream
        playlistStream.withReader { reader -> parser(reader) }
    }
}
