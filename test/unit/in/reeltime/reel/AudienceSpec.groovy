package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification

@TestFor(Audience)
@Mock([User])
class AudienceSpec extends Specification {

    void "audience can contain no members"() {
        when:
        def audience = new Audience()

        then:
        audience.validate()
    }

    void "audience contains one user"() {
        given:
        def user = new User().save()

        when:
        def audience = new Audience(users: [user])

        then:
        audience.validate()

        and:
        audience.users.size() == 1
        audience.users.contains(user)
    }

    void "audience contains many users"() {
        given:
        def user1 = new User().save()
        def user2 = new User().save()

        when:
        def audience = new Audience(users: [user1, user2])

        then:
        audience.validate()

        and:
        audience.users.size()  == 2

        and:
        audience.users.contains(user1)
        audience.users.contains(user2)
    }
}
