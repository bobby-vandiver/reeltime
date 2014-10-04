package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll
import test.helper.UserFactory
import in.reeltime.activity.ActivityType

class AudienceServiceIntegrationSpec extends IntegrationSpec {

    def audienceService
    def activityService

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

    @Unroll
    void "find all reels a user is an audience member of when user belongs to [#count] reels"() {
        given:
        def reels = createReels(count)
        def reelIds = reels*.id

        and:
        def memberUsername = 'member'
        def member = UserFactory.createUser(memberUsername)

        and:
        reelIds.each { reelId ->
            SpringSecurityUtils.doWithAuth(memberUsername) {
                audienceService.addMember(reelId)
            }
        }

        when:
        def reelsFollowed = audienceService.listReelsForAudienceMember(member)

        then:
        reelsFollowed.size() == count
        reelsFollowed == reels

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   5
        _   |   10
    }

    @Unroll
    void "remove user as an audience member from all reels when the user is an audience member of [#count] reels"() {
        given:
        def reels = createReels(count)
        def reelIds = reels*.id

        and:
        def memberUsername = 'member'
        def member = UserFactory.createUser(memberUsername)

        and:
        reelIds.each { reelId ->
            SpringSecurityUtils.doWithAuth(memberUsername) {
                audienceService.addMember(reelId)
            }
        }

        when:
        audienceService.removeMemberFromAllAudiences(member)

        then:
        Audience.findAllByAudienceMember(member).size() == 0

        and:
        assertReelsExist(reelIds)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   5
        _   |   10
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
        def member = UserFactory.createUser(memberUsername)

        when:
        SpringSecurityUtils.doWithAuth(memberUsername) {
            audienceService.addMember(reelId)
        }

        then:
        def audience = Audience.findByReel(reel)
        audience.members.size() == 1
        audience.members.contains(member)

        and:
        def activities = activityService.findActivities([member], [])
        activities.size() == 1

        activities[0].type == ActivityType.JoinReelAudience
        activities[0].user == member
        activities[0].reel == reel
    }

    void "the current user can remove themselves from an audience they are a member of"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def memberUsername = 'member'
        def member = UserFactory.createUser(memberUsername)

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

        and:
        def activities = activityService.findActivities([member], [])
        activities.size() == 0
    }

    void "the current user cannot remove themselves if they are not a member of the audience"() {
        given:
        def reel = createReelWithEmptyAudience()
        def reelId = reel.id

        and:
        def memberUsername = 'member'
        def member = UserFactory.createUser(memberUsername)

        and:
        def notMemberUsername = 'notMember'
        def notMember = UserFactory.createUser(notMemberUsername)

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
            def member = UserFactory.createUser("member$i")
            members.add(member)

            reel.audience.addToMembers(member)
            reel.save()
        }
        return members
    }

    private List<Reel> createReels(int count) {
        def reels = []
        count.times { it ->
            def user = UserFactory.createUser("someUser$it")
            reels << user.reels[0]
        }
        return reels
    }

    private Reel createReelWithEmptyAudience() {
        def user = UserFactory.createUser('foo')
        def reel = user.reels[0]
        assert reel.id > 0
        return reel
    }

    private static void assertReelsExist(Collection<Long> reelIds) {
        reelIds.each { reelId ->
            assert Reel.findById(reelId) != null
        }
    }
}
