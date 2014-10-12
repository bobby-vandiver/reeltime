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

    RestResponse registerUser(String username, String password, String clientName) {
        def email = username + '@test.com'
        def displayName = username
        def request = requestFactory.registerUser(username, password, displayName, email, clientName)

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to register user. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response
    }

    void removeAccount(String token) {
        def request = requestFactory.removeAccount(token)

        def response = delete(request)
        if(response.status != 200) {
            Assert.fail("Failed to remove access token")
        }

        response = delete(request)
        if(response.status != 401) {
            Assert.fail("The token associated with the deleted account is still valid! Status: ${response.status}")
        }
    }

    JSONElement newsfeed(String token) {
        def request = requestFactory.newsfeed(token)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to retrieve newsfeed. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    JSONElement listUsers(String token, Integer page = null) {
        def request = requestFactory.listUsers(token, page)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to retrieve list of users. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    void followUser(String token, String username) {
        def request = requestFactory.followUser(token, username)
        def response = post(request)

        if(response.status != 201) {
            Assert.fail("Failed to follow user: $username. Status code: ${response.status}. JSON: ${response.json}")
        }
    }

    void unfollowUser(String token, String username) {
        def request = requestFactory.unfollowUser(token, username)
        def response = delete(request)

        if(response.status != 200) {
            Assert.fail("Failed to unfollow user: $username. Status code: ${response.status}. JSON: ${response.json}")
        }
    }

    JSONElement listFollowers(String token, String username) {
        def request = requestFactory.listFollowers(token, username)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list followers for: $username. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    JSONElement listFollowees(String token, String username) {
        def request = requestFactory.listFollowees(token, username)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list followees for: $username. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    JSONElement listVideos(String token, Integer page = null) {
        def request = requestFactory.listVideos(token, page)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to retrieve list of videos. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    Long uploadVideoToUncategorizedReel(String token, String title = 'minimum-viable-video') {
        uploadVideoToReel(token, 'Uncategorized', title)
    }

    Long uploadVideoToReel(String token, String reel, String title) {
        def video = new File('test/files/small.mp4')

        def request = requestFactory.uploadVideo(token, title, reel, video)
        def response = post(request)

        if(response.status != 202) {
            Assert.fail("Failed to upload video. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json.videoId
    }

    int pollForCreationComplete(String uploadToken, long videoId,
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

    // TODO: Update existing invocations to specify username
    Long getUncategorizedReelId(String token, String username = 'bob') {
        def request = requestFactory.listReelsForUser(token, username)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to list reels. Status: ${response.status} JSON: ${response.json}")
        }
        def uncategorizedReel = response.json.find { it.name == 'Uncategorized' }
        return uncategorizedReel.reelId
    }

    JSONElement listReels(String token, Integer page = null) {
        def request = requestFactory.listReels(token, page)
        def response = get(request)

        if(response.status != 200) {
            Assert.fail("Failed to retrieve list of reels. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response.json
    }

    Long addReel(String token, String reelName) {
        def request = requestFactory.addReel(token, reelName)
        def response = post(request)

        if(response.status != 201) {
            Assert.fail("Failed to add reel [$reelName]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json.reelId
    }

    void deleteReel(String token, Long reelId) {
        def request = requestFactory.deleteReel(token, reelId)
        def response = delete(request)

        if(response.status != 200) {
            Assert.fail("Failed to delete reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }

    void addVideoToReel(String token, Long reelId, Long videoId) {
        def request = requestFactory.addVideoToReel(token, reelId, videoId)

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to add video [$videoId] to reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }

    JSONElement listVideosInReel(String token, Long reelId) {
        def request = requestFactory.listVideosInReel(token, reelId)

        def response = get(request)
        if(response.status != 200) {
            Assert.fail("Failed to list videos in reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json
    }

    JSONElement listAudienceMembers(String token, Long reelId) {
        def request = requestFactory.listAudienceMembers(token, reelId)

        def response = get(request)
        if(response.status != 200) {
            Assert.fail("Failed to list audience members for reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
        return response.json
    }

    void addAudienceMember(String token, Long reelId) {
        def request = requestFactory.addAudienceMember(token, reelId)

        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to add audience member for reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }

    void removeAudienceMember(String token, Long reelId) {
        def request = requestFactory.removeAudienceMember(token, reelId)

        def response = delete(request)
        if(response.status != 200) {
            Assert.fail("Failed to remove audience member for reel [$reelId]. Status: ${response.status} JSON: ${response.json}")
        }
    }
}
