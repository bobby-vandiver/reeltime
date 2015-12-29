package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri
import in.reeltime.playlist.PlaylistUriVideo
import in.reeltime.playlist.PlaylistVideo
import in.reeltime.thumbnail.Thumbnail
import in.reeltime.thumbnail.ThumbnailVideo
import in.reeltime.user.User
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Video)
@Mock([User, Thumbnail, Playlist, PlaylistUri, VideoCreator, ThumbnailVideo, PlaylistVideo, PlaylistUriVideo])
class VideoSpec extends Specification {

    void "valid video"() {
        when:
        def video = new Video(
                title: 'foo',
                masterPath: 'sample.mp4',
                masterThumbnailPath: 'sample.png'
        )

        then:
        video.validate()

        and:
        !video.available
        video.title == 'foo'
        video.masterPath == 'sample.mp4'
        video.masterThumbnailPath == 'sample.png'

        and:
        video.thumbnails.empty
        video.playlists.empty
        video.playlistUris.empty
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

    @Unroll
    void "#propertyName for #clazz with #joinClazz"() {
        given:
        def video = new Video().save(validate: false)
        def obj = clazz.newInstance().save(validate: false)

        joinClazz.newInstance(video: video, (joinClazzPropertyName): obj).save()

        expect:
        video."$propertyName".size() == 1
        video."$propertyName".contains(obj)

        where:
        propertyName    |   clazz           |   joinClazz           |   joinClazzPropertyName
        'thumbnails'    |   Thumbnail       |   ThumbnailVideo      |   'thumbnail'
        'playlists'     |   Playlist        |   PlaylistVideo       |   'playlist'
        'playlistUris'  |   PlaylistUri     |   PlaylistUriVideo    |   'playlistUri'
    }

    void "get creator"() {
        given:
        def video = new Video().save(validate: false)

        def user = new User()
        user.springSecurityService = Stub(SpringSecurityService)
        user.save(validate: false)

        and:
        new VideoCreator(video: video, creator: user).save()

        expect:
        video.creator == user
    }
}