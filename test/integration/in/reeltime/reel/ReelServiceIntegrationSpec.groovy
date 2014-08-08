package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.UserNotFoundException
import spock.lang.Unroll

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class ReelServiceIntegrationSpec extends IntegrationSpec {

    def reelService

    def userService
    def clientService

    User owner
    User notOwner

    void setup() {
        def ownerRequiredReel = reelService.createReel(UNCATEGORIZED_REEL_NAME)
        def ownerClient = clientService.createAndSaveClient('cname1', 'cid1', 'secret')
        owner = userService.createAndSaveUser('theOwner', 'password', 'someone@test.com', ownerClient, ownerRequiredReel)

        def nowOwnerRequiredReel = reelService.createReel(UNCATEGORIZED_REEL_NAME)
        def notOwnerClient = clientService.createAndSaveClient('cname2', 'cid2', 'secret')
        notOwner = userService.createAndSaveUser('notTheOwner', 'password', 'nobody@test.com', notOwnerClient, nowOwnerRequiredReel)
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

    @Unroll
    void "reel name [#name] is uncategorized [#truth]"() {
        expect:
        reelService.reelNameIsUncategorized(name) == truth

        where:
        name                            |   truth
        'Uncategorized'                 |   true
        'uncategorized'                 |   true
        'uNCatEgoriZED'                 |   true
        'UNCATEGORIZED'                 |   true
        'categorized'                   |   false
        'uncategorize'                  |   false
        'lionZ'                         |   false
        'TIgerS'                        |   false
        'BEARS'                         |   false
        'oh my'                         |   false
        'lions and tigers and bears'    |   false
    }

    void "cannot list reels for an unknown user"() {
        when:
        reelService.listReels('nobody')

        then:
        thrown(UserNotFoundException)
    }

    @Unroll
    void "list all reels belonging to specified user -- user has [#count] reels total"() {
        given:
        def reels = createReels(owner, count)

        when:
        def list = reelService.listReels(owner.username)

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
        def existingReelName = owner.reels[0].name
        def newReelName = existingReelName + 'a'

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(newReelName)
        }

        then:
        def retrieved = User.findByUsername(owner.username)
        retrieved.reels.size() == 2

        and:
        retrieved.reels.find { it.name == existingReelName } != null
        retrieved.reels.find { it.name == newReelName } != null
    }

    @Unroll
    void "do not allow a user to add a reel named [#uncategorized]"() {
        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addReel(uncategorized)
        }

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Reel name [$uncategorized] is reserved"

        where:
        _   |   uncategorized
        _   |   'Uncategorized'
        _   |   'uncategorized'
        _   |   'uNCatEgoriZED'
        _   |   'UNCATEGORIZED'
    }

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
