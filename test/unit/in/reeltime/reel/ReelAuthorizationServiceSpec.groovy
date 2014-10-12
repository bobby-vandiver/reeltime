package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.user.UserAuthenticationService
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ReelAuthorizationService)
class ReelAuthorizationServiceSpec extends Specification {

    UserAuthenticationService userAuthenticationService

    User owner
    User notOwner

    void setup() {
        userAuthenticationService = Mock(UserAuthenticationService)
        service.userAuthenticationService = userAuthenticationService

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
        ''                              |   false
        null                            |   false
    }

    @Unroll
    void "reel name [#name] is valid length [#valid]"() {
        expect:
        service.reelNameIsValidLength(name) == valid

        where:
        name        |   valid
        null        |   false
        ''          |   false
        'a'         |   false
        'a' * 4     |   false
        'a' * 5     |   true
        'a' * 25    |   true
        'a' * 26    |   false
    }

    void "reel owner is not the current user"() {
        given:
        def reel = new Reel(owner: owner)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        !result

        and:
        userAuthenticationService.currentUser >> notOwner
    }

    void "reel owner is the current user"() {
        given:
        def reel = new Reel(owner: owner)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        result

        and:
        userAuthenticationService.currentUser >> owner
    }
}
