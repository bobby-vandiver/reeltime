package in.reeltime.activity

import in.reeltime.FunctionalSpec
import spock.lang.Unroll

class NewsfeedFunctionalSpec extends FunctionalSpec {

    @Unroll
    void "invalid http methods for newsfeed endpoint"() {
        expect:
        responseChecker.assertInvalidHttpMethods(urlFactory.newsfeedUrl, ['post', 'put', 'delete'])
    }

    void "new user should have no activity because they are not following anything"() {
    }

    void "user is following a single reel"() {

    }

    void "user is following multiple reels"() {

    }

    void "user is following a single user"() {

    }

    void "user is following multiple users"() {

    }

    void "user is following a mix of users and reels"() {

    }
}
