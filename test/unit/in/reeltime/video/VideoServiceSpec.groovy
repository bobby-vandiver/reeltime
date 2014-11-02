package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.security.AuthenticationService
import in.reeltime.user.User
import in.reeltime.exceptions.VideoNotFoundException
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(VideoService)
@Mock([Video, User])
class VideoServiceSpec extends Specification {

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
}
