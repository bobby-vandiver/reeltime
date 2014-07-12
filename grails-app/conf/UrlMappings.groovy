class UrlMappings {

	static mappings = {

        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        "/available" (controller: 'applicationStatus', action: 'available')

        "/register" (controller: 'registration', action: 'register')

        "/confirm" (controller: 'registration', action: 'confirm')

        "/transcoder/notification/$action" (controller: 'notification')

        "/video" (controller: 'videoCreation', action: 'upload')

        "/video/$videoId/status" (controller: 'videoCreation', action: 'status')

        "/video/$videoId" (controller: 'playlist', action: 'getVariantPlaylist')

        "/video/$videoId/$playlistId" (controller: 'playlist', action: 'getMediaPlaylist')

        "/video/$videoId/$playlistId/$segmentId" (controller: 'segment', action: 'getSegment')
    }
}
