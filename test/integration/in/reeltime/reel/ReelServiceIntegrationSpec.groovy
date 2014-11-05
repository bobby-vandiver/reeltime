package in.reeltime.reel

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.exceptions.UserNotFoundException
import spock.lang.Unroll
import test.helper.ReelFactory
import test.helper.UserFactory

class ReelServiceIntegrationSpec extends IntegrationSpec {

    def reelService

    User owner
    User notOwner

    void setup() {
        owner = UserFactory.createUser('theOwner')
        notOwner = UserFactory.createUser('notTheOwner')
    }

    void "cannot list reels for an unknown user"() {
        when:
        reelService.listReelsByUsername('nobody', 1)

        then:
        thrown(UserNotFoundException)
    }

    @Unroll
    void "list all reels belonging to specified user -- user has [#count] reels total"() {
        given:
        def reels = createReels(owner, count)

        when:
        def list = reelService.listReelsByUsername(owner.username, 1)

        then:
        assertListsContainSameElements(list, reels)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
        _   |   10
    }

    void "list reels for user by page from newest to oldest"() {
        given:
        def savedMaxReelsPerPage = reelService.maxReelsPerPage
        reelService.maxReelsPerPage = 2

        and:
        def ownerUncategorizedReel = owner.reels[0]
        def someReel = ReelFactory.createReel(owner, 'some reel')
        def anotherReel = ReelFactory.createReel(owner, 'another reel')

        when:
        def pageOne = reelService.listReelsByUsername(owner.username, 1)

        then:
        pageOne.size() == 2

        and:
        pageOne[0] == anotherReel
        pageOne[1] == someReel

        when:
        def pageTwo = reelService.listReelsByUsername(owner.username, 2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == ownerUncategorizedReel

        cleanup:
        reelService.maxReelsPerPage = savedMaxReelsPerPage
    }

    void "list reels by page in order from newest to oldest"() {
        given:
        def savedMaxReelsPerPage = reelService.maxReelsPerPage
        reelService.maxReelsPerPage = 3

        and:
        def ownerUncategorizedReel = owner.reels[0]
        def notOwnerUncategorizedReel = notOwner.reels[0]

        and:
        def someReel = ReelFactory.createReel(owner, 'some reel')
        def anotherReel = ReelFactory.createReel(owner, 'another reel')

        when:
        def pageOne = reelService.listReels(1)

        then:
        pageOne.size() == 3

        and:
        pageOne[0] == anotherReel
        pageOne[1] == someReel
        pageOne[2] == notOwnerUncategorizedReel

        when:
        def pageTwo = reelService.listReels(2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == ownerUncategorizedReel

        cleanup:
        reelService.maxReelsPerPage = savedMaxReelsPerPage
    }

    private Collection<Reel> createReels(User owner, int count) {
        def reels = owner.reels
        def initialCount = reels.size()

        for(int i = initialCount; i < count; i++) {
            def reel = ReelFactory.createReel(owner, "reel $i")
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
