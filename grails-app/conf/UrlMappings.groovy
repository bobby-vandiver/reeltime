class UrlMappings {

	static mappings = {

        // TODO: Add dedicated error controller to handle exceptions and don't throw back to user
        "/"(view:"/index")
        "500"(view:'/error')

        "/transcoder/notification/$action" (controller: 'notification')

        "/video" (controller: 'videoCreation', action: 'upload')

        "/video/$videoId" (controller: 'playlist', action: 'getVariantPlaylist')

        "/video/$videoId/$playlistId" (controller: 'playlist', action: 'getMediaPlaylist')

        "/video/$videoId/$playlistId/$segmentId" (controller: 'segment', action: 'getSegment')
    }
}
