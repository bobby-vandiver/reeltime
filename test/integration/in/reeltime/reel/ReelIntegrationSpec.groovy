package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import spock.lang.Unroll
import test.helper.UserFactory
import test.helper.VideoFactory

class ReelIntegrationSpec extends IntegrationSpec {

    def reelVideoManagementService
    def audienceService

    User creator
    Reel reel

    void setup() {
        creator = UserFactory.createUser('creator')
        reel = creator.reels[0]
    }

    @Unroll
    void "get number of videos in reel when [#count] videos have been added"() {
        given:
        addVideosToReel(count)

        expect:
        reel.numberOfVideos == count

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   10
    }

    @Unroll
    void "get number of audience members when [#count] users are following reel"() {
        given:
        addUsersToAudience(count)

        expect:
        reel.numberOfAudienceMembers == count

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   10
    }

    private void addVideosToReel(int count) {
        count.times {
            SpringSecurityUtils.doWithAuth(creator.username) {
                def video = VideoFactory.createVideo(creator, "something-$it")
                reelVideoManagementService.addVideoToReel(reel, video)
            }
        }
    }

    private void addUsersToAudience(int count) {
        count.times {
            def user = UserFactory.createUser("user$it")
            SpringSecurityUtils.doWithAuth(user.username) {
                audienceService.addCurrentUserToAudience(reel.id)
            }
        }
    }
}
