package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Video)
class VideoSpec extends Specification {

    void "valid video"() {
        given:
        def user = new User()
        def playlist = new Playlist()
        def playlistUri = new PlaylistUri(uri: 'somewhere')

        when:
        def video = new Video(
                creator: user,
                title: 'foo',
                masterPath: 'sample.mp4',
                playlists: [playlist],
                playlistUris: [playlistUri],
        )

        then:
        video.validate()

        and:
        !video.available
        video.creator == user
        video.title == 'foo'
        video.masterPath == 'sample.mp4'
        video.playlists == [playlist] as Set
        video.playlistUris == [playlistUri] as Set
    }

    void "creator cannot be null (videos cannot be orphans)"() {
        when:
        def video = new Video(creator: null)

        then:
        !video.validate(['creator'])
    }

    @Unroll
    void "[#key] cannot be [#value]"() {
        when:
        def video = new Video((key): value)

        then:
        !video.validate([key])

        where:
        key                     |   value
        'title'                 |   ''
        'title'                 |   null
        'masterPath'            |   ''
        'masterPath'            |   null
        'masterThumbnailPath'   |   ''
        'masterThumbnailPath'   |   null
    }

    void "[#key] must be unique"() {
        given:
        def existingVideo = new Video((key): 'something')
        mockForConstraintsTests(Video, [existingVideo])

        when:
        def video = new Video((key): 'something')

        then:
        !video.validate([key])

        where:
        _   |   key
        _   |   'masterPath'
        _   |   'masterThumbnailPath'
    }
}