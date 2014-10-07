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
