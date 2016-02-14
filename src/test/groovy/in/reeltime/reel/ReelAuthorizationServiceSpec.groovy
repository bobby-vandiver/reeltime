package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ReelAuthorizationService)
@Mock([User, Reel, UserReel])
class ReelAuthorizationServiceSpec extends Specification {

    AuthenticationService authenticationService

    User owner
    User notOwner

    void setup() {
        authenticationService = Mock(AuthenticationService)
        service.authenticationService = authenticationService

        owner = createUser('owner')
        notOwner = createUser('notOwner')
    }

    private User createUser(String username) {
        User user = new User(username: username)
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)
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
        def reel = new Reel().save(validate: false)

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        !result

        and:
        authenticationService.currentUser >> notOwner
    }

    void "reel owner is the current user"() {
        given:
        def reel = new Reel().save(validate: false)
        new UserReel(owner: owner, reel: reel).save()

        when:
        def result = service.currentUserIsReelOwner(reel)

        then:
        result

        and:
        authenticationService.currentUser >> owner
    }
}
