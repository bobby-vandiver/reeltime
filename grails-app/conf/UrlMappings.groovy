class UrlMappings {

	static mappings = {

        /* Error handling */
        "405" (controller: 'errors', action: 'methodNotAllowed')
        "500" (controller: 'errors', action: 'internalServerError')

        /* Internal development use ONLY */
        "/internal/$username/confirm" (controller: 'developmentOnlyAccount') {
            action = [POST: 'confirmAccountForUser']
        }

        "/internal/$username/password" (controller: 'developmentOnlyAccount') {
            action = [POST: 'resetPasswordForUser']
        }

        /* AWS integration */
        "/aws/available" (controller: 'applicationStatus', action: 'available')

        "/aws/transcoder/notification" (controller: 'notification') {
            action = [POST: 'handleMessage']
        }

        /* ReelTime API */
        "/api/account" (controller: 'account') {
            action = [POST: 'registerAccount', DELETE: 'removeAccount']
        }

        "/api/account/client" (controller: 'clientManagement') {
            action = [POST: 'registerClient']
        }

        "/api/account/client/$client_id" (controller: 'clientManagement') {
            action = [DELETE: 'revokeClient']
        }

        "/api/account/confirm" (controller: 'accountConfirmation') {
            action = [POST:  'confirmAccount']
        }

        "/api/account/confirm/email" (controller: 'accountConfirmation') {
            action = [POST: 'sendEmail']
        }

        "/api/account/display_name" (controller: 'accountManagement') {
            action = [POST: 'changeDisplayName']
        }

        "/api/account/password" (controller: 'accountManagement') {
            action = [POST: 'changePassword']
        }

        "/api/account/password/email" (controller: 'resetPassword') {
            action = [POST: 'sendEmail']
        }

        "/api/account/password/reset" (controller: 'resetPassword') {
            action = [POST: 'resetPassword']
        }

        "/api/newsfeed" (controller: 'newsfeed') {
            action = [GET: 'listRecentActivity']
        }

        "/api/playlists/$video_id" (controller: 'playlist') {
            action = [GET: 'getVariantPlaylist']
        }

        "/api/playlists/$video_id/$playlist_id" (controller: 'playlist') {
            action = [GET: 'getMediaPlaylist']
        }

        "/api/playlists/$video_id/$playlist_id/$segment_id" (controller: 'segment') {
            action = [GET: 'getSegment']
        }

        "/api/reels" (controller: 'reel') {
            action = [GET: 'listReels', POST: 'addReel']
        }

        "/api/reels/$reel_id" (controller: 'reel') {
            action = [GET: 'getReel', DELETE: 'deleteReel']
        }

        "/api/reels/$reel_id/videos" (controller: 'reel') {
            action = [GET: 'listVideos', POST: 'addVideo']
        }

        "/api/reels/$reel_id/videos/$video_id" (controller: 'reel') {
            action = [DELETE: 'removeVideo']
        }

        "/api/reels/$reel_id/audience" (controller: 'audience') {
            action = [GET: 'listMembers', POST: 'addMember', DELETE: 'removeMember']
        }

        "/api/users" (controller: 'user') {
            action = [GET: 'listUsers']
        }

        "/api/users/$username" (controller: 'user') {
            action = [GET: 'getUser']
        }

        "/api/users/$username/reels" (controller: 'reel') {
            action = [GET: 'listUserReels']
        }

        "/api/users/$username/follow" (controller: 'userFollowing') {
            action = [POST: 'followUser', DELETE: 'unfollowUser']
        }

        "/api/users/$username/followers" (controller: 'userFollowing') {
            action = [GET: 'listFollowers']
        }

        "/api/users/$username/followees" (controller: 'userFollowing') {
            action = [GET: 'listFollowees']
        }

        "/api/videos" (controller: 'video') {
            action = [GET: 'listVideos', POST: 'upload']
        }

        "/api/videos/$video_id" (controller: 'video') {
            action = [GET: 'getVideo', DELETE: 'removeVideo']
        }
    }
}
