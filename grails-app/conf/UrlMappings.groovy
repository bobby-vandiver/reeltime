class UrlMappings {

	static mappings = {

        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        "/register" (controller: 'registration', action: 'register')

        "/verify" (controller: 'registration', action: 'verify')

        "/transcoder/notification/$action" (controller: 'notification')

        "/video" (controller: 'videoCreation', action: 'upload')

        "/video/$videoId/status" (controller: 'videoCreation', action: 'status')

        "/video/$videoId" (controller: 'playlist', action: 'getVariantPlaylist')

        "/video/$videoId/$playlistId" (controller: 'playlist', action: 'getMediaPlaylist')

        "/video/$videoId/$playlistId/$segmentId" (controller: 'segment', action: 'getSegment')
    }
}
