package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import spock.lang.Specification

@TestFor(ReelService)
@Mock([Reel, User])
class ReelServiceSpec extends Specification {

    void "create reel"() {
        given:
        def user = new User(username: 'someone')
        def reelName = 'awesome reel'

        when:
        def reel = service.createReel(user, reelName)

        then:
        reel.owner == user
        reel.name == reelName
        reel.audience.users.size() == 0
        reel.videos.size() == 0
    }
}
