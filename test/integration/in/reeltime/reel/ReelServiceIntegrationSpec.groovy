package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.exceptions.InvalidReelNameException
import spock.lang.Unroll
import test.helper.UserFactory
import in.reeltime.activity.ActivityType

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class ReelServiceIntegrationSpec extends IntegrationSpec {

    def reelService
    def activityService

    User owner
    User notOwner

    static final int TEST_MAX_REELS_PER_PAGE = 3
    int savedMaxReelsPerPage

    void setup() {
        owner = UserFactory.createUser('theOwner')
        notOwner = UserFactory.createUser('notTheOwner')

        savedMaxReelsPerPage = reelService.maxReelsPerPage
        reelService.maxReelsPerPage = TEST_MAX_REELS_PER_PAGE
    }

    void cleanup() {
        reelService.maxReelsPerPage = savedMaxReelsPerPage
    }

    void "create reel"() {
        given:
        def user = new User(username: 'someone')
        def reelName = 'awesome reel'

        when:
        def reel = reelService.createReelForUser(user, reelName)

        then:
        reel.owner == user
        reel.name == reelName
        reel.audience.members.size() == 0
    }

    void "do not allow a reel to be deleted if owner is not current user"() {
        given:
        def reelId = owner.reels[0].id

        when:
        SpringSecurityUtils.doWithAuth(notOwner.username) {
            reelService.deleteReel(reelId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Only the owner of a reel can delete it"
    }

    void "do not allow the uncategorized reel to be deleted"() {
        given:
        def reelId = owner.reels[0].id
        assert owner.reels[0].name == UNCATEGORIZED_REEL_NAME

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.deleteReel(reelId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "The Uncategorized reel cannot be deleted"
    }

    void "allow the owner to delete the reel"() {
        given:
        def name = 'another reel'

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(name)
        }

        and:
        def reelId = Reel.findByName(name).id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.deleteReel(reelId)
        }

        then:
        Reel.findById(reelId) == null
    }

    void "cannot list reels for an unknown user"() {
        when:
        reelService.listReelsByUsername('nobody')

        then:
        thrown(UserNotFoundException)
    }

    @Unroll
    void "list all reels belonging to specified user -- user has [#count] reels total"() {
        given:
        def reels = createReels(owner, count)

        when:
        def list = reelService.listReelsByUsername(owner.username)

        then:
        assertListsContainSameElements(list, reels)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
        _   |   10
        _   |   100
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
            newReel = reelService.addReel(newReelName)
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

        activities[0].type == ActivityType.CreateReel
        activities[0].user == owner
        activities[0].reel == newReel
    }

    void "do not allow a user to add a reel with the same name as an existing reel"() {
        given:
        def reelName = 'something'

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(reelName)
        }

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(reelName)
        }

        then:
        def e = thrown(InvalidReelNameException)
        e.message == "Reel named [$reelName] already exists"
    }

    @Unroll
    void "do not allow a user to add a reel named [#uncategorized]"() {
        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(uncategorized)
        }

        then:
        def e = thrown(InvalidReelNameException)
        e.message == "Reel name [$uncategorized] is reserved"

        where:
        _   |   uncategorized
        _   |   'Uncategorized'
        _   |   'uncategorized'
        _   |   'uNCatEgoriZED'
        _   |   'UNCATEGORIZED'
    }

    @Unroll
    void "do not allow a user to add a reel named [#name] that is an invalid length"() {
        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(name)
        }

        then:
        def e = thrown(InvalidReelNameException)
        e.message == "Reel name [$name] length is invalid"

        where:
        _   |   name
        _   |   null
        _   |   ''
        _   |   'a'
        _   |   'abc'
        _   |   'uhoh'
        _   |   'c' * 26
    }

//    void "list reels by page"() {
//        given:
//        def ownerUncategorizedReel = owner.reels[0]
//        def notOwnerUncategorizedReel = notOwner.reels[0]
//
//        and:
//        def someReel = ReelFactory.createReel(owner, 'some reel')
//        def anotherReel = ReelFactory.createReel(owner, 'another reel')
//
//        when:
//        def pageOne = reelService.listReels(1)
//
//        then:
//        pageOne.size() == TEST_MAX_REELS_PER_PAGE
//
//        and:
//        pageOne[0] == ownerUncategorizedReel
//        pageOne[1] == notOwnerUncategorizedReel
//        pageOne[2] == someReel
//
//        when:
//        def pageTwo = reelService.listReels()
//
//    }
//
    private Collection<Reel> createReels(User owner, int count) {
        def reels = owner.reels
        def initialCount = reels.size()

        for(int i = initialCount; i < count; i++) {
            def reel = reelService.createReelForUser(owner, "reel $i")
            reels << reel
            owner.addToReels(reel)
        }
        owner.save()
        return reels
    }

    private static void assertListsContainSameElements(Collection<?> actual, Collection<?> expected) {
        assert actual.size() == expected.size()

        expected.each { element ->
            assert actual.contains(element)
        }
    }
}
