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
        "/available" (controller: 'applicationStatus', action: 'available')

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

        /* ReelTime API */
        "/account" (controller: 'account') {
            action = [POST: 'registerAccount', DELETE: 'removeAccount']
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

        "/account/confirm/email" (controller: 'accountConfirmation') {
            action = [POST: 'sendEmail']
        }

        "/account/display_name" (controller: 'accountManagement') {
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

        "/playlists/$video_id" (controller: 'playlist') {
            action = [GET: 'getVariantPlaylist']
        }

        "/playlists/$video_id/$playlist_id" (controller: 'playlist') {
            action = [GET: 'getMediaPlaylist']
        }

        "/playlists/$video_id/$playlist_id/$segment_id" (controller: 'segment') {
            action = [GET: 'getSegment']
        }

        "/reels" (controller: 'reel') {
            action = [GET: 'listReels', POST: 'addReel']
        }

        "/reels/$reel_id" (controller: 'reel') {
            action = [GET: 'getReel', DELETE: 'deleteReel']
        }

        "/reels/$reel_id/videos" (controller: 'reel') {
            action = [GET: 'listVideos', POST: 'addVideo']
        }

        "/reels/$reel_id/videos/$video_id" (controller: 'reel') {
            action = [DELETE: 'removeVideo']
        }

        "/reels/$reel_id/audience" (controller: 'audience') {
            action = [GET: 'listMembers', POST: 'addMember', DELETE: 'removeMember']
        }

        "/users" (controller: 'user') {
            action = [GET: 'listUsers']
        }

        "/users/$username" (controller: 'user') {
            action = [GET: 'getUser']
        }

        "/users/$username/reels" (controller: 'reel') {
            action = [GET: 'listUserReels']
        }

        "/users/$username/follow" (controller: 'userFollowing') {
            action = [POST: 'followUser', DELETE: 'unfollowUser']
        }

        "/users/$username/followers" (controller: 'userFollowing') {
            action = [GET: 'listFollowers']
        }

        "/users/$username/followees" (controller: 'userFollowing') {
            action = [GET: 'listFollowees']
        }

        "/videos" (controller: 'video') {
            action = [GET: 'listVideos', POST: 'upload']
        }

        "/videos/$video_id" (controller: 'video') {
            action = [GET: 'getVideo', DELETE: 'removeVideo']
        }
    }
}
