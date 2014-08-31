package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.maintenance.ResourceRemovalService
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri
import in.reeltime.playlist.Segment
import in.reeltime.reel.Reel
import in.reeltime.reel.ReelVideoManagementService
import in.reeltime.user.User
import spock.lang.Specification

@TestFor(VideoRemovalService)
@Mock([Video, Playlist, PlaylistUri, Segment, User, Reel])
class VideoRemovalServiceSpec extends Specification {

    ReelVideoManagementService reelVideoManagementService
    ResourceRemovalService resourceRemovalService

    User creator

    void setup() {
        reelVideoManagementService = Mock(ReelVideoManagementService)
        service.reelVideoManagementService = reelVideoManagementService

        resourceRemovalService = Mock(ResourceRemovalService)
        service.resourceRemovalService = resourceRemovalService

        creator = new User(username: 'creator', password: 'secret')
        creator.springSecurityService = Stub(SpringSecurityService)
        creator.save(validate: false)
    }

    void "remove video successfully"() {
        given:
        def segment = new Segment(uri: 'seg1.ts').save(validate: false)
        def playlist = new Playlist(segments: [segment]).save(validate: false)

        def playlistUri = new PlaylistUri(uri: 'variant.m3u8').save(validate: false)
        def reel = new Reel().save(validate: false)

        def video = new Video(
                playlists: [playlist],
                playlistUris: [playlistUri],
                reels: [reel]
        ).save(validate: false)

        and:
        [segment, playlist, playlistUri, reel, video].each { assert it.id > 0 }

        when:
        service.removeVideo(video)

        then:
        1 * reelVideoManagementService.removeVideoFromAllReels(video)
    }
}
