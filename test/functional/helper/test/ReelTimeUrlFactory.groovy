package helper.test

public class ReelTimeUrlFactory {

    private String baseUrl

    ReelTimeUrlFactory(String baseUrl) {
        this.baseUrl = baseUrl
    }

    String getUrlForResource(String resource) {
        return baseUrl + resource
    }

    String getSpringSecurityCheckUrl() {
        getUrlForResource('j_spring_security_check')
    }

    String getInternalConfirmAccountUrl(username) {
        getUrlForResource("internal/$username/confirm")
    }

    String getInternalResetPasswordUrl(username) {
        getUrlForResource("internal/$username/password")
    }

    String getHealthCheckUrl() {
        getUrlForResource('available')
    }

    String getNotificationUrl() {
        getUrlForResource("transcoder/notification")
    }

    String getRegisterUrl() {
        getAccountUrl()
    }

    String getRemoveAccountUrl () {
        getAccountUrl()
    }

    String getAccountUrl() {
        getUrlForResource('account')
    }

    String getRegisterClientUrl() {
        getUrlForResource('account/client')
    }

    String getRevokeClientUrl(clientId) {
        getUrlForResource("account/client/$clientId")
    }

    String getConfirmationUrl() {
        getUrlForResource("account/confirm")
    }

    String getChangeDisplayNameUrl() {
        getUrlForResource('account/display_name')
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

    String getNewsfeedUrl() {
        getUrlForResource('newsfeed')
    }

    String getVariantPlaylistUrl(videoId) {
        getUrlForResource("playlists/$videoId")
    }

    String getMediaPlaylistUrl(videoId, playlistId) {
        getUrlForResource("playlists/$videoId/$playlistId")
    }

    String getSegmentUrl(videoId, playlistId, segmentId) {
        getUrlForResource("playlists/$videoId/$playlistId/$segmentId")
    }

    String getListVideosUrl() {
        getVideosUrl()
    }

    String getUploadUrl() {
        getVideosUrl()
    }

    String getVideosUrl() {
        getUrlForResource('videos')
    }

    String getDeleteVideoUrl(videoId) {
        getVideoUrl(videoId)
    }

    String getVideoUrl(videoId) {
        getUrlForResource("videos/$videoId")
    }

    String getListReelsUrl() {
        getReelsUrl()
    }

    String getAddReelUrl() {
        getReelsUrl()
    }

    String getReelsUrl() {
        getUrlForResource('reels')
    }

    String getReelUrl(reelId) {
        getUrlForResource("reels/$reelId")
    }

    String getDeleteReelUrl(reelId) {
        getReelUrl(reelId)
    }

    String getAddVideoToReelUrl(reelId) {
        getReelVideosUrl(reelId)
    }

    String getListVideosInReelUrl(reelId) {
        getReelVideosUrl(reelId)
    }

    private String getReelVideosUrl(reelId) {
        getUrlForResource("reels/$reelId/videos")
    }

    String getRemoveVideoFromReelUrl(reelId, videoId) {
        getUrlForResource("reels/$reelId/videos/$videoId")
    }

    String getAudienceUrl(reelId) {
        getUrlForResource("reels/$reelId/audience")
    }

    String getListUsersUrl() {
        getUrlForResource("users")
    }

    String getUserUrl(username) {
        getUrlForResource("users/$username")
    }

    String getReelsListUrl(username) {
        getUrlForResource("users/$username/reels")
    }

    String getFollowUrl(username) {
        getUrlForResource("users/$username/follow")
    }

    String getListFollowersUrl(username) {
        getUrlForResource("users/$username/followers")
    }

    String getListFolloweesUrl(username) {
        getUrlForResource("users/$username/followees")
    }
}