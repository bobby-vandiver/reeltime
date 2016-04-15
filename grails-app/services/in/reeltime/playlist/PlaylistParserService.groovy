package in.reeltime.playlist

import grails.transaction.Transactional
import in.reeltime.hls.playlist.parser.MediaPlaylistParser
import in.reeltime.hls.playlist.parser.VariantPlaylistParser

@Transactional
class PlaylistParserService {

    def playlistAndSegmentStorageService

    def maxRetries
    def intervalInMillis

    def parseVariantPlaylist(String path) {
        parse(path) { reader ->
            log.debug "Parsing variant playlist at [$path]"
            VariantPlaylistParser.parse(reader)
        }
    }

    def parseMediaPlaylist(String path) {
        parse(path) { reader ->
            log.debug "Parsing media playlist at [$path]"
            MediaPlaylistParser.parse(reader)
        }
    }

    private def parse(String path, Closure parser) {
        waitUntilPlaylistIsAvailable(path)

        log.debug("Parsing playlist at path [$path]")
        def playlistStream = playlistAndSegmentStorageService.load(path) as InputStream
        playlistStream.withReader { reader -> parser(reader) }
    }

    private void waitUntilPlaylistIsAvailable(String path) {
        int attempt = 0
        while(!playlistAndSegmentStorageService.exists(path) && attempt < maxRetries) {
            log.debug("Sleeping ${intervalInMillis} milliseconds until [$path] is available")
            sleep(intervalInMillis)
            attempt++
        }

        if(!playlistAndSegmentStorageService.exists(path)) {
            throw new IllegalArgumentException("[$path] does not exist!")
        }
    }
}
