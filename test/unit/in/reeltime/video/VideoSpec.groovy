package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.playlist.Playlist
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Video)
class VideoSpec extends Specification {

    void "valid video"() {
        given:
        def user = new User()
        def playlist = new Playlist()

        when:
        def video = new Video(
                creator: user,
                title: 'foo',
                description: 'bar',
                masterPath: 'sample.mp4',
                playlists: [playlist]
        )

        then:
        video.validate()

        and:
        !video.available
        video.creator == user
        video.title == 'foo'
        video.description == 'bar'
        video.masterPath == 'sample.mp4'
        video.playlists == [playlist] as Set
    }

    void "creator can be null (when the user has been removed)"() {
        when:
        def video = new Video(creator: null)

        then:
        video.validate(['creator'])
    }

    @Unroll
    void "title cannot be [#title]"() {
        when:
        def video = new Video(title: title)

        then:
        !video.validate(['title'])

        where:
        title << ['', null]
    }

    void "description can be blank"() {
        when:
        def video = new Video(description: '')

        then:
        video.validate(['description'])
    }

    @Unroll
    void "masterPath cannot be [#path]"() {
        when:
        def video = new Video(masterPath: path)

        then:
        !video.validate(['masterPath'])

        where:
        path << ['', null]
    }
}