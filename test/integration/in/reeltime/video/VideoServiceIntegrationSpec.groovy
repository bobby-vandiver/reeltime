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

    void "list videos in order of creation"() {
        given:
        def first = VideoFactory.createVideo(creator, 'first')
        def second = VideoFactory.createVideo(creator, 'second')

        when:
        def list = videoService.listVideos(1)

        then:
        list.size() == 2

        and:
        list[0] == first
        list[1] == second
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

    void "list videos by page"() {
        given:
        def first = VideoFactory.createVideo(creator, 'first')
        def second = VideoFactory.createVideo(creator, 'second')
        def third = VideoFactory.createVideo(creator, 'third')
        def fourth = VideoFactory.createVideo(creator, 'fourth')

        when:
        def pageOne = videoService.listVideos(1)

        then:
        pageOne.size() == 3

        and:
        pageOne[0] == first
        pageOne[1] == second
        pageOne[2] == third

        when:
        def pageTwo = videoService.listVideos(2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == fourth
    }
}
