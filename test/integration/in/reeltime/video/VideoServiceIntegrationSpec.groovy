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

    void "list videos by page from newest to oldest"() {
        given:
        def first = createVideoThenWait('first')
        def second = createVideoThenWait('second')
        def third = createVideoThenWait('third')
        def fourth = createVideoThenWait('fourth')

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

    private Video createVideoThenWait(String title) {
        def video = VideoFactory.createVideo(creator, title)
        sleep(500)
        return video
    }
}
