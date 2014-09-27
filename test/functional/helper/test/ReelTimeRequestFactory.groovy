package helper.test

import helper.rest.RestRequest

class ReelTimeRequestFactory {

    private ReelTimeUrlFactory urlFactory

    ReelTimeRequestFactory(ReelTimeUrlFactory urlFactory) {
        this.urlFactory = urlFactory
    }

    RestRequest registerUser(String name, String pass, String emailAddress, String client) {
        new RestRequest(url: urlFactory.registerUrl, customizer: {
            email = emailAddress
            username = name
            password = pass
            client_name = client
        })
    }

    RestRequest removeAccount(String token) {
        new RestRequest(url: urlFactory.removeAccountUrl, token: token)
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

    RestRequest listReels(String token, String username) {
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
}
