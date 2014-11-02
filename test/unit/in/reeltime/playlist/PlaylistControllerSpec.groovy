package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import in.reeltime.video.Video
import spock.lang.Unroll

@TestFor(PlaylistController)
@Mock([Video, Playlist])
class PlaylistControllerSpec extends Specification {

    void "return a 404 if the video does not exist"() {
        given:
        params.videoId = 1234

        when:
        controller.getVariantPlaylist()

        then:
        response.status == 404
    }

    void "return a 200 and the variant playlist for the requested video"() {
        given:
        def video = new Video(title: 'test', available: true).save(validate: false)
        params.videoId = video.id
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

    @Unroll
    void "return a 404 if [#name] does not exist"() {
        given:
        params."${name}" = 4982

        when:
        controller.getMediaPlaylist()

        then:
        response.status == 404

        where:
        name << ['playlist_id', 'video_id']
    }

    void "return a 404 if the playlist does not belong to the video"() {
        given:
        def playlist = new Playlist()
        def video1 = new Video(title: 'has playlist')

        video1.addToPlaylists(playlist)
        video1.save(validate: false)

        and:
        def video2 = new Video(title: 'no playlist').save(validate: false)

        and:
        assert playlist.id
        assert video1.id != video2.id

        and:
        params.videoId = video2.id
        params.playlist_id = playlist.id

        when:
        controller.getMediaPlaylist()

        then:
        response.status == 404
    }

    void "return a 200 and the media playlist for the requested video stream"() {
        given:
        def playlist = new Playlist()
        def video = new Video(title: 'has playlist', available: true)

        video.addToPlaylists(playlist)
        video.save(validate: false)

        and:
        assert playlist.id
        assert video.id

        and:
        params.videoId = video.id
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
        params.videoId = video.id
        assert video.id

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
