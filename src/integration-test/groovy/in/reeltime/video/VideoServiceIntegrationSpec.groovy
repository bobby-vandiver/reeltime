package in.reeltime.video

import grails.core.GrailsApplication
import grails.test.mixin.integration.Integration
import grails.test.runtime.DirtiesRuntime
import grails.transaction.Rollback
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.factory.VideoFactory
import in.reeltime.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class VideoServiceIntegrationSpec extends Specification {

    @Autowired
    VideoService videoService

    @Autowired
    GrailsApplication grailsApplication

    User creator

    static final int TEST_MAX_VIDEOS_PER_PAGE = 3

    @DirtiesRuntime
    void "do not list videos that are not available for streaming"() {
        given:
        setupCreator()
        changeMaxVideosPerPage(TEST_MAX_VIDEOS_PER_PAGE)

        and:
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

    @DirtiesRuntime
    void "list videos by page in order of creation from newest to oldest"() {
        given:
        setupCreator()
        changeMaxVideosPerPage(TEST_MAX_VIDEOS_PER_PAGE)

        and:
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

    private void setupCreator() {
        creator = UserFactory.createUser('creator')
    }

    private void changeMaxVideosPerPage(final int max) {
        videoService.maxVideosPerPage = max
    }

    private createAndAgeVideo(String title, int daysInFuture) {
        def video = VideoFactory.createVideo(creator, title)
        video.dateCreated = new Date() + daysInFuture
        video.save(flush: true)
        return video
    }
}
