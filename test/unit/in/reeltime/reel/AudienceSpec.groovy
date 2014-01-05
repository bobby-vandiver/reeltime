package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification

@TestFor(Audience)
class AudienceSpec extends Specification {

    private static final Reel IGNORE_REEL = new Reel()

    private Map args = [reel: IGNORE_REEL]

    void "audience belongs to a reel"() {
        given:
        def reel = new Reel()
        def audience = new Audience(reel: reel)

        expect:
        audience.validate()
    }

    void "audience can contain no members"() {
        when:
        def audience = new Audience(args)

        then:
        audience.validate()
    }

    void "audience contains one user"() {
        given:
        def user = new User(username: 'foo', password: 'bar')
        args << [users: [user]]

        when:
        def audience = new Audience(args)

        then:
        audience.validate()

        and:
        audience.users.size() == 1
        audience.users.contains(user)
    }

    void "audience contains many users"() {
        given:
        def user1 = new User(username: 'foo', password: 'bar')
        def user2 = new User(username: 'buzz', password: 'bazz')

        args << [users: [user1, user2]]

        when:
        def audience = new Audience(args)

        then:
        audience.validate()

        and:
        audience.users.size()  == 2

        and:
        audience.users.contains(user1)
        audience.users.contains(user2)
    }
}
