package in.reeltime.test.client

public class ReelTimeUrlFactory {

    private String baseUrl

    ReelTimeUrlFactory(String baseUrl) {
        this.baseUrl = baseUrl
    }

    String getUrlForApiResource(String resource) {
        return baseUrl + 'api/' + resource
    }

    String getUrlForWebResource(String resource) {
        return baseUrl + resource
    }

    String getUrlForInternalResource(String resource) {
        return baseUrl + 'internal/' + resource
    }

    String getUrlForAwsResource(String resource) {
        return baseUrl + 'aws/' + resource
    }

    String getSpringSecurityCheckUrl() {
        getUrlForWebResource('login/authenticate')
    }

    String getInternalConfirmAccountUrl(username) {
        getUrlForInternalResource("$username/confirm")
    }

    String getInternalResetPasswordUrl(username) {
        getUrlForApiResource("$username/password")
    }

    String getHealthCheckUrl() {
        getUrlForAwsResource('available')
    }

    String getNotificationUrl() {
        getUrlForAwsResource("transcoder/notification")
    }

    String getRegisterUrl() {
        getAccountUrl()
    }

    String getRemoveAccountUrl () {
        getAccountUrl()
    }

    String getAccountUrl() {
        getUrlForApiResource('account')
    }

    String getListClientsUrl() {
        getClientsUrl()
    }

    String getRegisterClientUrl() {
        getClientsUrl()
    }

    String getRevokeClientUrl(clientId) {
        getUrlForApiResource("account/clients/$clientId")
    }

    String getClientsUrl() {
        getUrlForApiResource('account/clients')
    }

    String getConfirmationUrl() {
        getUrlForApiResource("account/confirm")
    }

    String getChangeDisplayNameUrl() {
        getUrlForApiResource('account/display_name')
    }

    String getChangePasswordUrl() {
        getUrlForApiResource('account/password')
    }

    String getResetPasswordUrl() {
        getUrlForApiResource('account/password/reset')
    }

    String getSendResetPasswordEmailUrl() {
        getUrlForApiResource('account/password/reset/email')
    }

    String getNewsfeedUrl() {
        getUrlForApiResource('newsfeed')
    }

    String getVariantPlaylistUrl(videoId) {
        getUrlForApiResource("playlists/$videoId")
    }

    String getMediaPlaylistUrl(videoId, playlistId) {
        getUrlForApiResource("playlists/$videoId/$playlistId")
    }

    String getSegmentUrl(videoId, playlistId, segmentId) {
        getUrlForApiResource("playlists/$videoId/$playlistId/$segmentId")
    }

    String getTokenRevocationUrl() {
        getUrlForApiResource("tokens/revoke")
    }

    String getListVideosUrl() {
        getVideosUrl()
    }

    String getUploadUrl() {
        getVideosUrl()
    }

    String getVideosUrl() {
        getUrlForApiResource('videos')
    }

    String getDeleteVideoUrl(videoId) {
        getVideoUrl(videoId)
    }

    String getVideoUrl(videoId) {
        getUrlForApiResource("videos/$videoId")
    }

    String getThumbnailUrl(videoId) {
        getUrlForApiResource("videos/$videoId/thumbnail")
    }

    String getListReelsUrl() {
        getReelsUrl()
    }

    String getAddReelUrl() {
        getReelsUrl()
    }

    String getReelsUrl() {
        getUrlForApiResource('reels')
    }

    String getReelUrl(reelId) {
        getUrlForApiResource("reels/$reelId")
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
        getUrlForApiResource("reels/$reelId/videos")
    }

    String getRemoveVideoFromReelUrl(reelId, videoId) {
        getUrlForApiResource("reels/$reelId/videos/$videoId")
    }

    String getAudienceUrl(reelId) {
        getUrlForApiResource("reels/$reelId/audience")
    }

    String getListUsersUrl() {
        getUrlForApiResource("users")
    }

    String getUserUrl(username) {
        getUrlForApiResource("users/$username")
    }

    String getReelsListUrl(username) {
        getUrlForApiResource("users/$username/reels")
    }

    String getFollowUrl(username) {
        getUrlForApiResource("users/$username/follow")
    }

    String getListFollowersUrl(username) {
        getUrlForApiResource("users/$username/followers")
    }

    String getListFolloweesUrl(username) {
        getUrlForApiResource("users/$username/followees")
    }
}