package in.reeltime.video

import grails.test.spock.IntegrationSpec

class VideoIntegrationSpec extends IntegrationSpec {

    void "test deleting video deletes playlist"() {
        given:
        def user = new User().save()
        def playlist = new Playlist()

        and:
        final def vid = 7
        def video = new Video(videoId: vid, playlist: playlist, user: user, title: 'bar').save(flush: true)

        and:
        final def pid = playlist.id

        Video.findByVideoId(vid)
        Playlist.findById(pid)

        when:
        video.delete()

        then:
        !Video.findByVideoId(vid)
        !Playlist.findById(pid)
    }
}
