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
        def reel = new Reel()

        when:
        def video = new Video(
                creator: user,
                title: 'foo',
                description: 'bar',
                masterPath: 'sample.mp4',
                playlists: [playlist],
                playlistUris: [playlistUri],
                reels: [reel]
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
        video.playlistUris == [playlistUri] as Set
        video.reels == [reel] as Set
    }

    void "creator cannot be null (videos cannot be orphans)"() {
        when:
        def video = new Video(creator: null)

        then:
        !video.validate(['creator'])
    }

    @Unroll
    void "reels cannot be [#reels]"() {
        when:
        def video = new Video(reels: reels)

        then:
        !video.validate(['reels'])

        where:
        reels << [null, []]
    }

    void "video can belong to multiple reels"() {
        given:
        def reel1 = new Reel()
        def reel2 = new Reel()

        when:
        def video = new Video(reels: [reel1, reel2])

        then:
        video.validate(['reels'])
    }

    void "add reel to reels video belongs to"() {
        given:
        def reel = new Reel()
        def video = new Video()

        when:
        video.addToReels(reel)

        then:
        video.reels.size() == 1
        video.reels.contains(reel)
    }

    void "remove reel from reels video belongs to"() {
        given:
        def reel = new Reel()
        def video = new Video(reels: [reel])

        when:
        video.removeFromReels(reel)

        then:
        video.reels.size() == 0
        !video.reels.contains(reel)
    }

    void "attempt to remove reel that video does not belong to"() {
        given:
        def reel = new Reel()
        def video = new Video()

        when:
        video.removeFromReels(reel)

        then:
        notThrown(Exception)
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