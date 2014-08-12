class UrlMappings {

	static mappings = {

        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        "/available" (controller: 'applicationStatus', action: 'available')

        "/account/register" (controller: 'account', action: 'register')

        "/account/confirm" (controller: 'account', action: 'confirm')

        "/transcoder/notification/$action" (controller: 'notification')

        "/video" (controller: 'videoCreation', action: 'upload')

        "/video/$videoId/status" (controller: 'videoCreation', action: 'status')

        "/video/$videoId" (controller: 'playlist', action: 'getVariantPlaylist')

        "/video/$videoId/$playlistId" (controller: 'playlist', action: 'getMediaPlaylist')

        "/video/$videoId/$playlistId/$segmentId" (controller: 'segment', action: 'getSegment')

        "/user/$username/reels" (controller: 'reel', action: 'listReels')

        "/reel" (controller: 'reel', action: 'addReel')

        "/reel/$reelId" (controller: 'reel') {
            action = [GET: 'listVideos', POST: 'addVideo', DELETE: 'deleteReel']
        }
    }
}
