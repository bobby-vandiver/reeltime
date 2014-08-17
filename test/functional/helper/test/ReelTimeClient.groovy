package helper.test

import grails.plugins.rest.client.RestResponse
import helper.rest.AuthorizationAwareRestClient
import junit.framework.Assert
import org.codehaus.groovy.grails.web.json.JSONElement

class ReelTimeClient {

    @Delegate
    private AuthorizationAwareRestClient restClient

    private ReelTimeRequestFactory requestFactory

    // Video creation completion polling defaults
    private static final DEFAULT_MAX_POLL_COUNT = 12
    private static final DEFAULT_RETRY_DELAY_IN_MILLIS = 5 * 1000

    ReelTimeClient(AuthorizationAwareRestClient restClient, ReelTimeRequestFactory requestFactory) {
        this.restClient = restClient
        this.requestFactory = requestFactory
    }

    RestResponse registerUser(String username, String password = 'password', String clientName = 'client') {
        def email = username + '@test.com'
        def request = requestFactory.registerUser(username, password, email, clientName)

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to register user. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response
    }

    long uploadVideo(String token) {
        def title = 'minimum-viable-video'
        def video = new File('test/files/small.mp4')

        def request = requestFactory.uploadVideo(token, title, video)
        def response = post(request)

        if(response.status != 202) {
            Assert.fail("Failed to upload video. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json.videoId
    }

    int pollForCreationComplete(long videoId, String uploadToken,
              int maxPollCount = DEFAULT_MAX_POLL_COUNT, long retryDelayMillis = DEFAULT_RETRY_DELAY_IN_MILLIS) {

        def request = requestFactory.videoStatus(uploadToken, videoId)

        int videoCreatedStatus = 0
        int pollCount = 0

        while(videoCreatedStatus != 201 && pollCount < maxPollCount) {
            def response = get(request)
            videoCreatedStatus = response.status

            if(videoCreatedStatus == 202) {
                println "Video [$videoId] is still being created. Sleeping for 5 seconds before next status query."
                sleep(retryDelayMillis)
            }
            pollCount++
        }
        return videoCreatedStatus
    }

    // TODO: Specify username
    Long getUncategorizedReelId(String token) {
        def request = requestFactory.listReels(token, 'bob')
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list reels. Status: ${response.status} JSON: ${response.json}")
        }
        def uncategorizedReel = response.json.find { it.name == 'Uncategorized' }
        return uncategorizedReel.reelId
    }

    JSONElement listAudienceMembers(Long reelId, String token) {
        def request = requestFactory.listAudienceMembers(token, reelId)

        def response = get(request)
        if(response.status != 200) {
            Assert.fail("Failed to list audience members for reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json
    }

    void addAudienceMember(Long reelId, String token) {
        def request = requestFactory.addAudienceMember(token, reelId)

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to add audience member for reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }

    void removeAudienceMember(Long reelId, String token) {
        def request = requestFactory.removeAudienceMember(token, reelId)

        def response = delete(request)
        if(response.status != 200) {
            Assert.fail("Failed to remove audience member for reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }
}
