package in.reeltime.video

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import test.helper.AutoTimeStampSuppressor
import test.helper.UserFactory
import test.helper.VideoFactory

class VideoServiceIntegrationSpec extends IntegrationSpec {

    def videoService
    def grailsApplication

    User creator

    static final int TEST_MAX_VIDEOS_PER_PAGE = 3
    int savedMaxVideosPerPage

    AutoTimeStampSuppressor timeStampSuppressor

    void setup() {
        creator = UserFactory.createUser('creator')

        savedMaxVideosPerPage = videoService.maxVideosPerPage
        videoService.maxVideosPerPage = TEST_MAX_VIDEOS_PER_PAGE

        timeStampSuppressor = new AutoTimeStampSuppressor(grailsApplication: grailsApplication)
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
        def first = createAndAgeVideo('first', 0)
        def second = createAndAgeVideo('second', 1)
        def third = createAndAgeVideo('third', 2)
        def fourth = createAndAgeVideo('fourth', 3)

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

    private createAndAgeVideo(String title, int daysInFuture) {
        def video = VideoFactory.createVideo(creator, title)
        timeStampSuppressor.withAutoTimestampSuppression(video) {
            video.dateCreated = new Date() + daysInFuture
            video.save()
        }
        return video
    }
}
