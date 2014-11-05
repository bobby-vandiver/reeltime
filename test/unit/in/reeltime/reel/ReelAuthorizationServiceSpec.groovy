package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.security.AuthenticationService
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ReelAuthorizationService)
class ReelAuthorizationServiceSpec extends Specification {

    AuthenticationService authenticationService

    User owner
    User notOwner

    void setup() {
        authenticationService = Mock(AuthenticationService)
        service.authenticationService = authenticationService

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

    void "reel owner is not the current user"() {
        given:
        def reel = new Reel(owner: owner)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        !result

        and:
        authenticationService.currentUser >> notOwner
    }

    void "reel owner is the current user"() {
        given:
        def reel = new Reel(owner: owner)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        result

        and:
        authenticationService.currentUser >> owner
    }
}
