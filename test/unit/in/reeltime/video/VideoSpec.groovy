package in.reeltime.video

import grails.test.mixin.TestFor
import in.reeltime.playlist.PlaylistUri
import in.reeltime.reel.Reel
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
    void "title cannot be [#title]"() {
        when:
        def video = new Video(title: title)

        then:
        !video.validate(['title'])

        where:
        title << ['', null]
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