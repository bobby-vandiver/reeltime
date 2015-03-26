package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri
import in.reeltime.thumbnail.Thumbnail
import in.reeltime.thumbnail.ThumbnailResolution
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
        def thumbnail = new Thumbnail(uri: 'anywhere', resolution: ThumbnailResolution.RESOLUTION_1X)

        when:
        def video = new Video(
                creator: user,
                title: 'foo',
                masterPath: 'sample.mp4',
                masterThumbnailPath: 'sample.png',
                thumbnails: [thumbnail],
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
        video.masterThumbnailPath == 'sample.png'
        video.thumbnails == [thumbnail] as Set
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
    void "[#key] can be empty"() {
        when:
        def video = new Video((key): [] as Set)

        then:
        video.validate([key])

        where:
        _   |   key
        _   |   'thumbnails'
        _   |   'playlists'
        _   |   'playlistUris'
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