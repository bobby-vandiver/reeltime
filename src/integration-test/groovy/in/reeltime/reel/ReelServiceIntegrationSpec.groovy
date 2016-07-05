package in.reeltime.reel

import grails.core.GrailsApplication
import grails.test.mixin.integration.Integration
import grails.test.runtime.DirtiesRuntime
import grails.transaction.Rollback
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.test.factory.ReelFactory
import in.reeltime.test.factory.UserFactory
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class ReelServiceIntegrationSpec extends Specification {

    @Autowired
    ReelService reelService

    @Autowired
    GrailsApplication grailsApplication

    User owner
    User notOwner

    void "cannot list reels for an unknown user"() {
        when:
        reelService.listReelsByUsername('nobody', 1)

        then:
        thrown(UserNotFoundException)
    }

    @Unroll
    void "list all reels belonging to specified user -- user has [#count] reels total"() {
        given:
        createOwner()

        and:
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

    @DirtiesRuntime
    void "list reels for user by page from newest to oldest"() {
        given:
        createOwner()

        and:
        changeMaxReelsPerPage(2)

        and:
        def ownerUncategorizedReel = owner.reels[0]

        def someReel = ReelFactory.createReel(owner, 'some reel')
        def anotherReel = ReelFactory.createReel(owner, 'another reel')

        and:
        ageReel(ownerUncategorizedReel, 0)
        ageReel(someReel, 1)
        ageReel(anotherReel, 2)

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
    }

    @DirtiesRuntime
    void "list reels by page in order from newest to oldest"() {
        given:
        createOwner()
        createNotOwner()

        and:
        changeMaxReelsPerPage(3)

        and:
        def ownerUncategorizedReel = owner.reels[0]
        def notOwnerUncategorizedReel = notOwner.reels[0]

        and:
        def someReel = ReelFactory.createReel(owner, 'some reel')
        def anotherReel = ReelFactory.createReel(owner, 'another reel')

        and:
        ageReel(ownerUncategorizedReel, 0)
        ageReel(notOwnerUncategorizedReel, 1)

        ageReel(someReel, 2)
        ageReel(anotherReel, 3)

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
    }

    private void changeMaxReelsPerPage(final int max) {
        reelService.maxReelsPerPage = max
    }

    private void createOwner() {
        owner = UserFactory.createUser('theOwner')
    }

    private void createNotOwner() {
        notOwner = UserFactory.createUser('notTheOwner')
    }

    private Collection<Reel> createReels(User owner, int count) {
        def reels = owner.reels
        def initialCount = reels.size()

        for(int i = initialCount; i < count; i++) {
            def reel = ReelFactory.createReel(owner, "reel $i")
            ageReel(reel, i)
            reels << reel
        }
        return reels
    }

    private static void ageReel(Reel reel, int daysInTheFuture) {
        reel.dateCreated = new Date() + daysInTheFuture
        reel.save(flush: true)
    }

    private static void assertListsContainSameElements(Collection<?> actual, Collection<?> expected) {
        assert actual.size() == expected.size()

        expected.each { element ->
            assert actual.contains(element)
        }
    }
}
