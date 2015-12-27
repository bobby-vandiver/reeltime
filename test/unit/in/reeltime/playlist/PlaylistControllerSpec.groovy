package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.video.Video
import spock.lang.Unroll

@TestFor(PlaylistController)
@Mock([Video, Playlist, PlaylistVideo])
class PlaylistControllerSpec extends AbstractControllerSpec {

    void "return a 404 if the video does not exist"() {
        given:
        params.video_id = 1234

        when:
        controller.getVariantPlaylist()

        then:
        response.status == 404
    }

    void "return a 200 and the variant playlist for the requested video"() {
        given:
        def video = new Video(title: 'test', available: true).save(validate: false)
        params.video_id = video.id
        assert video.id

        and:
        controller.playlistService = Mock(PlaylistService)

        when:
        controller.getVariantPlaylist()

        then:
        1 * controller.playlistService.generateVariantPlaylist({it.id == video.id}) >> 'variant playlist'

        and:
        response.status == 200
        response.contentType == 'application/x-mpegURL'
        response.contentAsString == 'variant playlist'
    }

    void "return a 404 if the playlist does not belong to the video"() {
        given:
        def playlist = new Playlist().save()
        def video1 = new Video(title: 'has playlist').save(validate: false)

        new PlaylistVideo(playlist: playlist, video: video1).save()

        and:
        def video2 = new Video(title: 'no playlist').save(validate: false)

        and:
        assert playlist.id
        assert video1.id != video2.id

        and:
        params.video_id = video2.id
        params.playlist_id = playlist.id

        when:
        controller.getMediaPlaylist()

        then:
        response.status == 404
    }

    void "return a 200 and the media playlist for the requested video stream"() {
        given:
        def playlist = new Playlist().save()
        def video = new Video(title: 'has playlist', available: true).save(validate: false)

        new PlaylistVideo(playlist: playlist, video: video).save()

        and:
        assert playlist.id
        assert video.id

        and:
        params.video_id = video.id
        params.playlist_id = playlist.id

        assert Playlist.findById(params.playlist_id)

        and:
        controller.playlistService = Mock(PlaylistService)

        when:
        controller.getMediaPlaylist()

        then:
        1 * controller.playlistService.generateMediaPlaylist({it.id == playlist.id}, true) >> 'media playlist'

        and:
        response.status == 200
        response.contentType == 'application/x-mpegURL'
        response.contentAsString == 'media playlist'
    }

    @Unroll
    void "return a 404 if the video exists but is not available for [#method]"() {
        given:
        def video = new Video(title: 'test', available: false).save(validate: false)
        params.video_id = video.id
        assert video.id

        params.playlist_id = 1234

        when:
        controller."$method"()

        then:
        response.status == 404

        where:
        _   |   method
        _   |   'getVariantPlaylist'
        _   |   'getMediaPlaylist'
    }
}
