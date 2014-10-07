package in.reeltime.search

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video
import spock.lang.Unroll

@TestFor(SearchController)
class SearchControllerSpec extends AbstractControllerSpec {

    UserSearchService userSearchService
    VideoSearchService videoSearchService
    ReelSearchService reelSearchService

    void setup() {
        userSearchService = Mock(UserSearchService)
        videoSearchService = Mock(VideoSearchService)
        reelSearchService = Mock(ReelSearchService)

        controller.userSearchService = userSearchService
        controller.videoSearchService = videoSearchService
        controller.reelSearchService = reelSearchService
    }

    void "no user results for type user"() {
        given:
        params.type = 'user'
        params.query = 'something'

        when:
        controller.search()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.results.size() == 0

        and:
        1 * userSearchService.search('something', 1) >> new SearchResult<User>(results: [], query: 'something')
    }

    void "search for user returns results"() {
        given:
        params.type = 'user'
        params.query = 'something'

        and:
        def user1 = new User(username: 'user1', displayName: 'display1')
        def user2 = new User(username: 'user2', displayName: 'display2')

        def users = [user1, user2]

        when:
        controller.search()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)

        def results = json.results
        results.size() == 2

        and:
        results[0].username == 'user1'
        results[0].display_name == 'display1'

        and:
        results[1].username == 'user2'
        results[1].display_name == 'display2'

        and:
        1 * userSearchService.search('something', 1) >> new SearchResult<User>(results: users, query: 'something')
    }

    void "no video results for type video"() {
        given:
        params.type = 'video'
        params.query = 'something'

        when:
        controller.search()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.results.size() == 0

        and:
        1 * videoSearchService.search('something', 1) >> new SearchResult<Video>(results: [], query: 'something')
    }

    void "no reel results for type reel"() {
        given:
        params.type = 'reel'
        params.query = 'something'

        when:
        controller.search()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.results.size() == 0

        and:
        1 * reelSearchService.search('something', 1) >> new SearchResult<Reel>(results: [], query: 'something')
    }
}
