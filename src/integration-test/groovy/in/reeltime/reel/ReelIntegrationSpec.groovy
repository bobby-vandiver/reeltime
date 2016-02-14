package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.factory.VideoFactory

@Integration
@Rollback
class ReelIntegrationSpec extends Specification {

    @Autowired
    ReelVideoManagementService reelVideoManagementService

    @Autowired
    AudienceService audienceService

    User creator
    Reel reel

    @Unroll
    void "get number of videos in reel when [#count] videos have been added"() {
        given:
        setupData()

        and:
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
        setupData()

        and:
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

    void "current user is an audience member"() {
        given:
        setupData()

        and:
        UserFactory.createUser('current')

        expect:
        SpringSecurityUtils.doWithAuth('current') {
            audienceService.addCurrentUserToAudience(reel.id)
            reel.currentUserIsAnAudienceMember
        }
    }

    void "current user is not an audience member"() {
        given:
        setupData()

        and:
        UserFactory.createUser('current')

        expect:
        SpringSecurityUtils.doWithAuth('current') {
            !reel.currentUserIsAnAudienceMember
        }
    }

    private void setupData() {
        creator = UserFactory.createUser('creator')
        reel = creator.reels[0]
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
