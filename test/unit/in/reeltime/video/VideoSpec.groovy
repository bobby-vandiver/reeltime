package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.user.User
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
        def video = new Video(creator: user, title: 'foo', description: 'bar', playlists: [playlist])

        then:
        video.validate()

        and:
        video.creator == user
        video.title == 'foo'
        video.description == 'bar'
        video.playlists == [playlist] as Set
    }

    void "creator can be null (when the user has been removed)"() {
        given:
        mockForConstraintsTests(Video)

        when:
        def video = new Video(creator: null, title: 'ignore')

        then:
        video.validate()
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

    void "description can be blank"() {
        given:
        mockForConstraintsTests(Video)

        when:
        def video = new Video(title: 'ignore', description: '')

        then:
        video.validate()
    }
}