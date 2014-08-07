package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.exceptions.VideoNotFoundException
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(VideoService)
@Mock([Video, User])
class VideoServiceSpec extends Specification {

    SpringSecurityService springSecurityService

    void setup() {
        springSecurityService = Mock(SpringSecurityService)
        service.springSecurityService = springSecurityService
    }

    void "creator is the current user"() {
        given:
        def creator = createUser('creator')
        def video = new Video(creator: creator).save(validate: false)

        when:
        def result = service.currentUserIsVideoCreator(video.id)

        then:
        result

        and:
        1 * springSecurityService.currentUser >> creator
    }

    void "creator is not the current user"() {
        given:
        def creator = createUser('creator')
        def video = new Video(creator: creator).save(validate: false)

        and:
        def currentUser = createUser('current')

        when:
        def result = service.currentUserIsVideoCreator(video.id)

        then:
        !result

        and:
        1 * springSecurityService.currentUser >> currentUser
    }

    void "video exists"() {
        given:
        def video = new Video().save(validate: false)

        expect:
        service.videoExists(video.id)
    }

    void "video does not exist"() {
        expect:
        !service.videoExists(1234)
    }

    void "load video that exists"() {
        given:
        def video = new Video().save(validate: false)

        and:
        def videoId = video.id
        assert videoId > 0

        expect:
        service.loadVideo(videoId) != null
    }

    void "throw if unable to load video"() {
        when:
        service.loadVideo(1234)

        then:
        def e = thrown(VideoNotFoundException)
        e.message == 'Video [1234] not found'
    }

    void "unknown video is never available"() {
        expect:
        !service.videoIsAvailable(123458)
    }

    @Unroll
    void "video is available [#available]"() {
        given:
        def video = new Video(available: available).save(validate: false)

        expect:
        service.videoIsAvailable(video.id) == available

        where:
        _   |   available
        _   |   true
        _   |   false
    }

    private User createUser(String username) {
        def user = new User(username: username)
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)
        return user
    }
}
