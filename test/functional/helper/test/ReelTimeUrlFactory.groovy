package helper.test

public class ReelTimeUrlFactory {

    private String baseUrl

    ReelTimeUrlFactory(String baseUrl) {
        this.baseUrl = baseUrl
    }

    String getUrlForResource(String resource) {
        return baseUrl + resource
    }

    String getRegisterUrl() {
        getUrlForResource('account/register')
    }

    String getRegisterClientUrl() {
        getUrlForResource('account/client')
    }

    String getVerifyUrl() {
        getUrlForResource("account/confirm")
    }

    String getRemoveAccountUrl () {
        getUrlForResource('account')
    }

    String getNewsfeedUrl() {
        getUrlForResource('newsfeed')
    }

    String getUploadUrl() {
        getUrlForResource('video')
    }

    String getDeleteVideoUrl(videoId) {
        getUrlForResource("video/$videoId")
    }

    String getStatusUrl(videoId) {
        getUrlForResource("video/$videoId/status")
    }

    String getNotificationUrl(action) {
        getUrlForResource("transcoder/notification/$action")
    }

    String getReelsListUrl(username) {
        getUrlForResource("/user/$username/reels")
    }

    String getAddReelUrl() {
        getUrlForResource('reel')
    }

    String getReelUrl(reelId) {
        getUrlForResource("reel/$reelId")
    }

    String getRemoveVideoFromReelUrl(reelId, videoId) {
        getUrlForResource("reel/$reelId/$videoId")
    }

    String getAudienceUrl(reelId) {
        getUrlForResource("reel/$reelId/audience")
    }

    String getHealthCheckUrl() {
        getUrlForResource('available')
    }

    String getSpringSecurityCheckUrl() {
        getUrlForResource('j_spring_security_check')
    }
}