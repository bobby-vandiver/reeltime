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
        audience.validate(['users'])
    }

    void "audience contains one user"() {
        given:
        def user = new User(username: 'foo', password: 'bar')

        when:
        def audience = new Audience(users: [user])

        then:
        audience.validate(['users'])

        and:
        audience.users.size() == 1
        audience.users.contains(user)
    }

    void "audience contains many users"() {
        given:
        def user1 = new User(username: 'foo', password: 'bar')
        def user2 = new User(username: 'buzz', password: 'bazz')

        when:
        def audience = new Audience(users: [user1, user2])

        then:
        audience.validate(['users'])

        and:
        audience.users.size()  == 2

        and:
        audience.users.contains(user1)
        audience.users.contains(user2)
    }
}
