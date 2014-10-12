class UrlMappings {

	static mappings = {

        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        "/available" (controller: 'applicationStatus', action: 'available')

        "/account" (controller: 'account') {
            action = [DELETE: 'removeAccount']
        }

        "/account/register" (controller: 'account') {
            action = [POST: 'register']
        }

        "/account/client" (controller: 'account') {
            action = [POST: 'registerClient']
        }

        "/account/confirm" (controller: 'account') {
            action = [POST:  'confirm']
        }

        "/newsfeed" (controller: 'newsfeed') {
            action = [GET: 'listRecentActivity']
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

        "/videos" (controller: 'video') {
            action = [GET: 'listVideos']
        }

        "/video" (controller: 'videoCreation') {
            action = [POST: 'upload']
        }

        "/video/$videoId" (controller: 'videoRemoval') {
            action = [DELETE: 'remove']
        }

        "/video/$videoId/status" (controller: 'videoCreation') {
            action = [GET: 'status']
        }

        "/playlist/$videoId" (controller: 'playlist') {
            action = [GET: 'getVariantPlaylist']
        }

        "/playlist/$videoId/$playlistId" (controller: 'playlist') {
            action = [GET: 'getMediaPlaylist']
        }

        "/playlist/$videoId/$playlistId/$segmentId" (controller: 'segment') {
            action = [GET: 'getSegment']
        }

        "/users" (controller: 'user') {
            action = [GET: 'listUsers']
        }

        "/user/$username/reels" (controller: 'reel') {
            action = [GET: 'listUserReels']
        }

        "/user/$username/follow" (controller: 'userFollowing') {
            action = [POST: 'followUser', DELETE: 'unfollowUser']
        }

        "/user/$username/followers" (controller: 'userFollowing') {
            action = [GET: 'listFollowers']
        }

        "/user/$username/followees" (controller: 'userFollowing') {
            action = [GET: 'listFollowees']
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
