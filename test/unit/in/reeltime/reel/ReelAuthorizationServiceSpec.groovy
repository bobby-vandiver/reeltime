package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.user.UserService
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ReelAuthorizationService)
class ReelAuthorizationServiceSpec extends Specification {

    UserService userService

    User owner
    User notOwner

    void setup() {
        userService = Mock(UserService)
        service.userService = userService

        owner = new User(username:'owner')
        notOwner = new User(username:'notOwner')
    }

    @Unroll
    void "reel name [#name] is reserved [#truth]"() {
        expect:
        service.reelNameIsReserved(name) == truth

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

    void "reel owner is not the current user"() {
        given:
        def reel = new Reel(owner: owner)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        !result

        and:
        userService.currentUser >> notOwner
    }

    void "reel owner is the current user"() {
        given:
        def reel = new Reel(owner: owner)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        result

        and:
        userService.currentUser >> owner
    }
}
