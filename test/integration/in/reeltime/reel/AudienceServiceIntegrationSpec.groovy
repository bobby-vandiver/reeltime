package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.user.User
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll

class AudienceServiceIntegrationSpec extends IntegrationSpec {

    def reelService
    def audienceService

    @Unroll
    void "list audience with [#count] members"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def membersAdded = addAudienceMembersToReel(reel, count)

        when:
        def list = audienceService.listMembers(reelId)

        then:
        list.size() == count

        for(int i = 0; i < list.size(); i++) {
            def member = list.getAt(i)
            assert membersAdded.contains(member)
        }

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   10
        _   |   50
    }

    void "the owner of the reel cannot add themselves to the audience"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def ownerUsername = reel.owner.username

        when:
        SpringSecurityUtils.doWithAuth(ownerUsername) {
            audienceService.addMember(reelId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Owner of a reel cannot be a member of the reel's audience"

        and:
        def audience = Audience.findByReel(reel)
        audience.members.size() == 0
    }

    void "add the current user as an audience member"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def memberUsername = 'member'
        def member = createUser(memberUsername, 'clientId')

        when:
        SpringSecurityUtils.doWithAuth(memberUsername) {
            audienceService.addMember(reelId)
        }

        then:
        def audience = Audience.findByReel(reel)
        audience.members.size() == 1
        audience.members.contains(member)
    }

    void "the current user can remove themselves from an audience they are a member of"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def memberUsername = 'member'
        createUser(memberUsername, 'clientId')

        and:
        SpringSecurityUtils.doWithAuth(memberUsername) {
            audienceService.addMember(reelId)
        }

        and:
        assert Audience.findByReel(reel).members.size() == 1

        when:
        SpringSecurityUtils.doWithAuth(memberUsername) {
            audienceService.removeMember(reelId)
        }

        then:
        def audience = Audience.findByReel(reel)
        audience.members.size() == 0
    }

    void "the current user cannot remove themselves if they are not a member of the audience"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def memberUsername = 'member'
        def member = createUser(memberUsername, 'clientId')

        and:
        def notMemberUsername = 'notMember'
        def notMember = createUser(notMemberUsername, 'anotherClientId')

        and:
        SpringSecurityUtils.doWithAuth(memberUsername) {
            audienceService.addMember(reelId)
        }

        and:
        assert Audience.findByReel(reel).members.size() == 1

        when:
        SpringSecurityUtils.doWithAuth(notMemberUsername) {
            audienceService.removeMember(reelId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Current user [$notMemberUsername] is not a member of the audience for reel [$reelId]"

        and:
        def audience = Audience.findByReel(reel)
        audience.members.size() == 1

        and:
        audience.members.contains(member)
        !audience.members.contains(notMember)
    }

    private Collection<User> addAudienceMembersToReel(Reel reel, int count) {
        def members = []

        for(int i = 0; i < count; i++) {
            def member = createUser("member$i", "clientId$i")
            members.add(member)

            reel.audience.addToMembers(member)
            reel.save()
        }
        return members
    }

    private Reel createReelWithEmptyAudience() {
        def user = createUser('foo', 'bar')
        def reel = user.reels[0]
        assert reel.id > 0
        return reel
    }

    private User createUser(String username, String clientId) {
        def client = new Client(clientId: clientId, clientSecret: 'secret', clientName: 'name').save()
        def reel = reelService.createReel('test reel')

        new User(username: "$username", password: 'password', email: "$username@test.com")
                .addToClients(client)
                .addToReels(reel)
                .save()
    }
}
