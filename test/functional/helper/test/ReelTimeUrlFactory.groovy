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

    String getRevokeClientUrl(clientId) {
        getUrlForResource("account/client/$clientId")
    }

    String getVerifyUrl() {
        getUrlForResource("account/confirm")
    }

    String getChangeDisplayNameUrl() {
        getUrlForResource('account/display')
    }

    String getChangePasswordUrl() {
        getUrlForResource('account/password')
    }

    String getSendResetPasswordEmailUrl() {
        getUrlForResource('account/password/email')
    }

    String getResetPasswordUrl() {
        getUrlForResource('account/password/reset')
    }

    String getInternalConfirmAccountUrl(username) {
        getUrlForResource("internal/$username/confirm")
    }

    String getInternalResetPasswordUrl(username) {
        getUrlForResource("internal/$username/password")
    }

    String getRemoveAccountUrl () {
        getUrlForResource('account')
    }

    String getNewsfeedUrl() {
        getUrlForResource('newsfeed')
    }

    String getListVideosUrl() {
        getUrlForResource('videos')
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

    String getUserUrl(username) {
        getUrlForResource("user/$username")
    }

    String getListUsersUrl() {
        getUrlForResource("users")
    }

    String getReelsListUrl(username) {
        getUrlForResource("user/$username/reels")
    }

    String getFollowUrl(username) {
        getUrlForResource("user/$username/follow")
    }

    String getListFollowersUrl(username) {
        getUrlForResource("user/$username/followers")
    }

    String getListFolloweesUrl(username) {
        getUrlForResource("user/$username/followees")
    }

    String getListReelsUrl() {
        getUrlForResource('reels')
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