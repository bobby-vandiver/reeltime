package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.activity.ActivityService
import in.reeltime.activity.ActivityType
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll
import in.reeltime.test.factory.UserFactory

@Integration
@Rollback
class ReelCreationServiceIntegrationSpec extends Specification {

    @Autowired
    ReelCreationService reelCreationService

    @Autowired
    ActivityService activityService

    User owner

   void "create reel"() {
        given:
        def reelName = 'awesome reel'

        when:
        def reel = reelCreationService.createAndSaveReel(reelName)

        then:
        reel.name == reelName
        reel.audience.size() == 0
    }

    @Unroll
    void "add new reel to current user"() {
        given:
        createOwner()

        and:
        def existingReel = owner.reels[0]
        def newReel = null

        and:
        def existingReelName = existingReel.name
        def newReelName = existingReelName + 'a'

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            newReel = reelCreationService.addReel(newReelName)
        }

        then:
        def retrieved = User.findByUsername(owner.username)
        retrieved.reels.size() == 2

        and:
        retrieved.reels.contains(existingReel)
        retrieved.reels.contains(newReel)

        and:
        def activities = activityService.findActivities([retrieved], [])
        activities.size() == 1

        activities[0].type == ActivityType.CreateReel.value
        activities[0].user == owner
        activities[0].reel == newReel
    }

    void "do not allow a user to add a reel with the same name as an existing reel"() {
        given:
        createOwner()

        and:
        def reelName = 'something'

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelCreationService.addReel(reelName)
        }

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelCreationService.addReel(reelName)
        }

        then:
        def e = thrown(InvalidReelNameException)
        e.message == "Reel named [$reelName] already exists"
    }

    private void createOwner() {
        owner = UserFactory.createUser('theOwner')
    }
}
