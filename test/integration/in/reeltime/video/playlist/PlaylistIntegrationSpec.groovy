package in.reeltime.video.playlist

import grails.plugin.spock.IntegrationSpec
import in.reeltime.video.Video

class PlaylistIntegrationSpec extends IntegrationSpec {

    void "deleting playlist deletes children segments"() {
        given:
        def segment = new Segment(segmentId: 1, uri: 'foo', duration: '1.0')

        def playlist = new Playlist(codecs: 'heh', resolution: 'hah')
        playlist.addToSegments(segment)

        def video = new Video(title: 'ignore')
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
