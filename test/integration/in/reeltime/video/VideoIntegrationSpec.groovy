package in.reeltime.video

import grails.test.spock.IntegrationSpec
import in.reeltime.playlist.Playlist
import test.helper.UserFactory

class VideoIntegrationSpec extends IntegrationSpec {

    void "deleting video deletes playlist"() {
        given:
        def playlist = new Playlist(codecs: 'buzz', resolution: 'bazz')
        def creator = UserFactory.createTestUser()

        def video = new Video(creator: creator, title: 'bar', masterPath: 'foo')
        video.addToPlaylists(playlist)
        video.save()

        and:
        def videoId = video.id
        def playlistId = playlist.id

        assert Video.findById(videoId)
        assert Playlist.findById(playlistId)

        when:
        video.delete()

        then:
        !Video.findById(videoId)
        !Playlist.findById(playlistId)
    }
}
