package in.reeltime.playlist

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
import test.helper.UserFactory

class PlaylistIntegrationSpec extends IntegrationSpec {

    void "deleting playlist deletes children segments"() {
        given:
        def segment = new Segment(segmentId: 1, uri: 'foo', duration: '1.0')

        def playlist = new Playlist(codecs: 'heh', resolution: 'hah')
        playlist.addToSegments(segment)

        def user = UserFactory.createTestUser()
        def reel = user.reels[0]

        def video = new Video(creator: user, title: 'ignore', masterPath: 'ignore', reels: [reel])
        video.addToPlaylists(playlist)
        video.save()

        def segmentId = segment.id
        def playlistId = playlist.id

        assert Segment.findById(segmentId)
        assert Playlist.findById(playlistId)

        when:
        video.delete()

        then:
        !Playlist.findById(playlistId)
        !Segment.findById(segmentId)
    }
}
