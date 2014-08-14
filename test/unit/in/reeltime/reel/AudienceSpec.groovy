package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification

@TestFor(Audience)
class AudienceSpec extends Specification {

    void "audience belongs to a reel"() {
        given:
        def reel = new Reel()
        def audience = new Audience(reel: reel)

        expect:
        audience.validate()
    }

    void "audience can contain no members"() {
        when:
        def audience = new Audience()

        then:
        audience.validate(['members'])
    }

    void "audience contains one member"() {
        given:
        def user = new User(username: 'foo', password: 'bar')

        when:
        def audience = new Audience(members: [user])

        then:
        audience.validate(['members'])

        and:
        audience.members.size() == 1
        audience.members.contains(user)
    }

    void "audience contains many members"() {
        given:
        def user1 = new User(username: 'foo', password: 'bar')
        def user2 = new User(username: 'buzz', password: 'bazz')

        when:
        def audience = new Audience(members: [user1, user2])

        then:
        audience.validate(['members'])

        and:
        audience.members.size()  == 2

        and:
        audience.members.contains(user1)
        audience.members.contains(user2)
    }

    void "audience does not have member"() {
        given:
        def notMember = new User(username: 'nobody', password: 'secret')
        def audience = new Audience()

        expect:
        !audience.hasMember(notMember)
    }

    void "audience has member"() {
        given:
        def member = new User(username: 'somebody', password: 'secret')
        def audience = new Audience(members: [member])

        expect:
        audience.hasMember(member)
    }
}
