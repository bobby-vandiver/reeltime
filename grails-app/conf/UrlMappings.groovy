class UrlMappings {

	static mappings = {

        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        "/available" (controller: 'applicationStatus', action: 'available')

        "/account" (controller: 'account') {
            action = [DELETE: 'removeAccount']
        }

        "/account/register" (controller: 'account') {
            action = [POST: 'registerAccount']
        }

        "/account/client" (controller: 'clientManagement') {
            action = [POST: 'registerClient']
        }

        "/account/client/$client_id" (controller: 'clientManagement') {
            action = [DELETE: 'revokeClient']
        }

        "/account/confirm" (controller: 'accountConfirmation') {
            action = [POST:  'confirmAccount']
        }

        "/account/display" (controller: 'accountManagement') {
            action = [POST: 'changeDisplayName']
        }

        "/account/password" (controller: 'accountManagement') {
            action = [POST: 'changePassword']
        }

        "/account/password/email" (controller: 'resetPassword') {
            action = [POST: 'sendEmail']
        }

        "/account/password/reset" (controller: 'resetPassword') {
            action = [POST: 'resetPassword']
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

        "/reels" (controller: 'reel') {
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

        "/internal/$username/confirm" (controller: 'developmentOnlyAccount') {
            action = [POST: 'confirmAccountForUser']
        }

        "/internal/$username/password" (controller: 'developmentOnlyAccount') {
            action = [POST: 'resetPasswordForUser']
        }
    }
}
