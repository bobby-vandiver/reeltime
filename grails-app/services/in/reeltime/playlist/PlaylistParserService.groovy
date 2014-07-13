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
        waitUntilPlaylistIsAvailable(path)

        log.debug("Parsing playlist at path [$path]")
        def playlistStream = outputStorageService.load(path) as InputStream
        playlistStream.withReader { reader -> parser(reader) }
    }

    private void waitUntilPlaylistIsAvailable(String path) {
        final MAX_RETRIES = 5
        final INTERVAL_IN_MILLS = 2000

        int attempt = 0
        while(!outputStorageService.exists(path) && attempt < MAX_RETRIES) {
            log.debug("Sleeping ${INTERVAL_IN_MILLS} milliseconds until [$path] is available")
            sleep(INTERVAL_IN_MILLS)
            attempt++
        }
    }
}
