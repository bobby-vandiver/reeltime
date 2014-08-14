class UrlMappings {

	static mappings = {

        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        "/available" (controller: 'applicationStatus', action: 'available')

        "/account/register" (controller: 'account') {
            action = [POST: 'register']
        }

        "/account/confirm" (controller: 'account') {
            action = [POST:  'confirm']
        }

        "/transcoder/notification/completed" (controller: 'notification') {
            action = [POST: 'completed']
        }

        "/transcoder/notification/progressing" (controller: 'notification') {
            action = [POST: 'progressing']
        }

        "/transcoder/notification/warning" (controller: 'notification') {
            action = [POST: 'warning']
        }

        "/transcoder/notification/error" (controller: 'notification') {
            action = [POST: 'error']
        }

        "/video" (controller: 'videoCreation') {
            action = [POST: 'upload']
        }

        "/video/$videoId/status" (controller: 'videoCreation') {
            action = [GET: 'status']
        }

        "/video/$videoId" (controller: 'playlist') {
            action = [GET: 'getVariantPlaylist']
        }

        "/video/$videoId/$playlistId" (controller: 'playlist') {
            action = [GET: 'getMediaPlaylist']
        }

        "/video/$videoId/$playlistId/$segmentId" (controller: 'segment') {
            action = [GET: 'getSegment']
        }

        "/user/$username/reels" (controller: 'reel') {
            action = [GET: 'listReels']
        }

        "/reel" (controller: 'reel') {
            action = [POST: 'addReel']
        }

        "/reel/$reelId" (controller: 'reel') {
            action = [GET: 'listVideos', POST: 'addVideo', DELETE: 'deleteReel']
        }

        "/reel/$reelId/$videoId" (controller: 'reel') {
            action = [DELETE: 'removeVideo']
        }

        "/reel/$reelId/audience" (controller: 'audience') {
            action = [GET: 'listMembers', POST: 'addMember', DELETE: 'removeMember']
        }
    }
}
