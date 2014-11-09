package in.reeltime.video

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import test.helper.UserFactory
import test.helper.VideoFactory

class VideoServiceIntegrationSpec extends IntegrationSpec {

    def videoService

    User creator

    static final int TEST_MAX_VIDEOS_PER_PAGE = 3
    int savedMaxVideosPerPage

    void setup() {
        creator = UserFactory.createUser('creator')

        savedMaxVideosPerPage = videoService.maxVideosPerPage
        videoService.maxVideosPerPage = TEST_MAX_VIDEOS_PER_PAGE
    }

    void cleanup() {
        videoService.maxVideosPerPage = savedMaxVideosPerPage
    }

    void "do not list videos that are not available for streaming"() {
        given:
        def exclude = VideoFactory.createVideo(creator, 'exclude', false)
        def include = VideoFactory.createVideo(creator, 'include', true)

        when:
        def list = videoService.listVideos(1)

        then:
        list.size() == 1

        and:
        list.contains(include)
        !list.contains(exclude)
    }

    void "list videos by page in order of creation from newest to oldest"() {
        given:
        def first = VideoFactory.createVideoAndWait(creator, 'first', 500)
        def second = VideoFactory.createVideoAndWait(creator, 'second', 500)
        def third = VideoFactory.createVideoAndWait(creator, 'third', 500)
        def fourth = VideoFactory.createVideoAndWait(creator, 'fourth', 500)

        when:
        def pageOne = videoService.listVideos(1)

        then:
        pageOne.size() == 3

        and:
        pageOne[0] == fourth
        pageOne[1] == third
        pageOne[2] == second

        when:
        def pageTwo = videoService.listVideos(2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == first
    }
}
