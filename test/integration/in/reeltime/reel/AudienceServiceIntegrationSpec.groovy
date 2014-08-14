package in.reeltime.reel

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.user.User
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
