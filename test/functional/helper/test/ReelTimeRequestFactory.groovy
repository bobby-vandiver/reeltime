package helper.test

import helper.rest.RestRequest

class ReelTimeRequestFactory {

    private ReelTimeUrlFactory urlFactory

    ReelTimeRequestFactory(ReelTimeUrlFactory urlFactory) {
        this.urlFactory = urlFactory
    }

    RestRequest registerUser(String name, String pass, String display, String emailAddress, String client) {
        new RestRequest(url: urlFactory.registerUrl, customizer: {
            email = emailAddress
            username = name
            password = pass
            display_name = display
            client_name = client
        })
    }

    RestRequest removeAccount(String token) {
        new RestRequest(url: urlFactory.removeAccountUrl, token: token)
    }

    RestRequest sendResetPasswordEmail(String name) {
        new RestRequest(url: urlFactory.registerUrl, customizer: {
            username = name
        })
    }

    RestRequest resetPasswordForRegisteredClient(String name, String pass, String resetCode,
                                                 String clientId, String clientSecret) {
        new RestRequest(url: urlFactory.resetPasswordUrl, customizer: {
            username = name
            new_password = pass
            code = resetCode
            client_is_registered = true
            client_id = clientId
            client_secret = clientSecret
        })
    }

    RestRequest resetPasswordForNewClient(String name, String pass, String resetCode, String clientName) {
        new RestRequest(url: urlFactory.resetPasswordUrl, customizer: {
            username = name
            new_password = pass
            code = resetCode
            client_is_registered = false
            client_name = clientName
        })
    }

    RestRequest confirmAccountForUser(String token, String username) {
        def url = urlFactory.getInternalConfirmAccountUrl(username)
        new RestRequest(url: url, token: token)
    }

    RestRequest resetPasswordForUser(String token, String username, String password) {
        def url = urlFactory.getInternalResetPasswordUrl(username)
        new RestRequest(url: url, token: token, customizer: {
            new_password = password
        })
    }

    RestRequest newsfeed(String token) {
        new RestRequest(url: urlFactory.newsfeedUrl, token: token)
    }

    RestRequest listUsers(String token, Integer pageNumber) {
        paginatedListRequest(token, pageNumber, urlFactory.listUsersUrl)
    }

    RestRequest followUser(String token, String username) {
        followUserRequest(token, username)
    }

    RestRequest unfollowUser(String token, String username) {
        followUserRequest(token, username)
    }

    private RestRequest followUserRequest(String token, String username) {
        def url = urlFactory.getFollowUrl(username)
        new RestRequest(url: url, token: token)
    }

    RestRequest listFollowers(String token, String username) {
        def url = urlFactory.getListFollowersUrl(username)
        new RestRequest(url: url, token: token)
    }

    RestRequest listFollowees(String token, String username) {
        def url = urlFactory.getListFolloweesUrl(username)
        new RestRequest(url: url, token: token)
    }

    RestRequest listVideos(String token, Integer pageNumber) {
        paginatedListRequest(token, pageNumber, urlFactory.listVideosUrl)
    }

    RestRequest uploadVideo(String token, String videoTitle, String reelName, File videoFile) {
        new RestRequest(url: urlFactory.uploadUrl, token: token, isMultiPart: true, customizer: {
            title = videoTitle
            reel = reelName
            video = videoFile
        })
    }

    RestRequest deleteVideo(String token, Long vid) {
        def url = urlFactory.getDeleteVideoUrl(vid)
        new RestRequest(url: url, token: token, customizer: {
            videoId = vid
        })
    }

    RestRequest videoStatus(String token, Long videoId) {
        def url = urlFactory.getStatusUrl(videoId)
        new RestRequest(url: url, token: token)
    }

    RestRequest listReels(String token, Integer pageNumber) {
        paginatedListRequest(token, pageNumber, urlFactory.listReelsUrl)
    }

    RestRequest listReelsForUser(String token, String username) {
        def url = urlFactory.getReelsListUrl(username)
        new RestRequest(url: url, token: token)
    }

    RestRequest addReel(String token, String reelName) {
        new RestRequest(url: urlFactory.addReelUrl, token: token, customizer: {
            name = reelName
        })
    }

    RestRequest deleteReel(String token, Long reelId) {
        def url = urlFactory.getReelUrl(reelId)
        new RestRequest(url: url, token: token)
    }

    RestRequest addVideoToReel(String token, Long reelId, Long vid) {
        def url = urlFactory.getReelUrl(reelId)
        new RestRequest(url: url, token: token, customizer: {
            videoId = vid
        })
    }

    RestRequest listVideosInReel(String token, Long reelId) {
        def url = urlFactory.getReelUrl(reelId)
        new RestRequest(url: url, token: token)
    }

    RestRequest listAudienceMembers(String token, Long reelId) {
        audienceRequest(token, reelId)
    }

    RestRequest addAudienceMember(String token, Long reelId) {
        audienceRequest(token, reelId)
    }

    RestRequest removeAudienceMember(String token, Long reelId) {
        audienceRequest(token, reelId)
    }

    private RestRequest audienceRequest(String token, Long reelId) {
        def url = urlFactory.getAudienceUrl(reelId)
        new RestRequest(url: url, token: token)
    }

    private RestRequest paginatedListRequest(String token, Integer pageNumber, String url) {
        new RestRequest(url: url, token: token, queryParams: [page: pageNumber ?: 1])
    }
}
