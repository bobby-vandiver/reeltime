package in.reeltime.search

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
import test.helper.UserFactory

class VideoSearchServiceIntegrationSpec extends IntegrationSpec {

    def videoSearchService

    User creator

    void setup() {
        creator = UserFactory.createUser('creator')
    }

    void "include original query in search results"() {
        when:
        def searchResult = videoSearchService.search('test', 1)

        then:
        searchResult.query == 'test'
    }

    void "empty video search result when no videos in system"() {
        when:
        def searchResult = videoSearchService.search('something', 1)

        then:
        searchResult.results.size() == 0
    }

    void "search for videos by title"() {
        given:
        def video1 = createVideo("Optimus Prime")
        def video2 = createVideo("prime banana mashers")
        def video3 = createVideo("fruit salad cheesecake")
        def video4 = createVideo("completely unrelated")
        def video5 = createVideo("cheese burger sandwich")

        when:
        def searchResult = videoSearchService.search('prime', 1)

        then:
        searchResult.results.contains(video1)
        searchResult.results.contains(video2)

        and:
        !searchResult.results.contains(video3)
        !searchResult.results.contains(video4)
        !searchResult.results.contains(video5)
    }

    private Video createVideo(String title) {
        new Video(creator: creator, title: title, masterPath: 'somewhere').save()
    }
}
