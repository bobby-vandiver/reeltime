package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.video.playlist.Playlist
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Video)
class VideoSpec extends Specification {

    private static final IGNORE_TITLE = 'ignoreTitle'
    private static final IGNORE_MASTER_PATH = 'ignoreMasterPath'

    private Map args = [title: IGNORE_TITLE, masterPath: IGNORE_MASTER_PATH]

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
        video.creator == user
        video.title == 'foo'
        video.description == 'bar'
        video.masterPath == 'sample.mp4'
        video.playlists == [playlist] as Set
    }

    void "creator can be null (when the user has been removed)"() {
        given:
        args << [creator: null]

        when:
        def video = new Video(args)

        then:
        video.validate()
    }

    @Unroll
    void "title cannot be [#title]"() {
        given:
        args << [title: title]

        when:
        def video = new Video(args)

        then:
        !video.validate()

        where:
        title << ['', null]
    }

    void "description can be blank"() {
        given:
        args << [description: '']

        when:
        def video = new Video(args)

        then:
        video.validate()
    }

    @Unroll
    void "masterPath cannot be [#path]"() {
        given:
        args << [masterPath: path]

        when:
        def video = new Video(args)

        then:
        !video.validate()

        where:
        path << ['', null]
    }
}