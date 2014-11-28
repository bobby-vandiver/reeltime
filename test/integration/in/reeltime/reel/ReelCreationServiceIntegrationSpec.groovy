package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import spock.lang.Unroll
import test.helper.UserFactory
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.activity.ActivityType

class ReelCreationServiceIntegrationSpec extends IntegrationSpec {

    def reelCreationService
    def activityService

    User owner
    User notOwner

    void setup() {
        owner = UserFactory.createUser('theOwner')
        notOwner = UserFactory.createUser('notTheOwner')
    }

    void "create reel"() {
        given:
        def user = new User(username: 'someone')
        def reelName = 'awesome reel'

        when:
        def reel = reelCreationService.createReelForUser(user, reelName)

        then:
        reel.owner == user
        reel.name == reelName
        reel.audience.members.size() == 0
    }

    @Unroll
    void "add new reel to current user"() {
        given:
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
}
