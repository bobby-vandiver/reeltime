package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.video.playlist.Playlist
import spock.lang.Specification

@TestFor(Video)
class VideoSpec extends Specification {

    void "valid video"() {
        given:
        mockForConstraintsTests(Video)

        def user = new User()
        def playlist = new Playlist()

        when:
        def video = new Video(user: user, videoId: 3, title: 'foo', playlist: playlist)

        then:
        video.validate()

        and:
        video.user == user
        video.videoId == 3
        video.title == 'foo'
        video.playlist == playlist
        video.status == Video.ConversionStatus.SUBMITTED
    }

    void "title cannot be blank"() {
        given:
        mockForConstraintsTests(Video)

        when:
        def video = new Video(title: '')

        then:
        !video.validate()

        and:
        video.errors['title'] == 'blank'
    }

}