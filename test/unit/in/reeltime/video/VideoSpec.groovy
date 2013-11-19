package in.reeltime.video

import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(Video)
class VideoSpec extends Specification {

    void "valid video"() {
        given:
        mockForConstraintsTests(Video)

        def user = new User()
        def playlist = new Playlist()

        when:
        def video = new Video(user: user, videoId: 3, title: 'foo', playlist: playlist)

        then:
        video.validate()

        and:
        video.user == user
        video.videoId == 3
        video.title == 'foo'
        video.playlist == playlist
    }

    void "videoId must be unique"() {
        given:
        def user = new User()
        def playlist = new Playlist()

        def existingVideo = new Video(videoId: 1, title: 'ignore', playlist: playlist, user: user )
        mockForConstraintsTests(Video, [existingVideo])

        when:
        def duplicate = new Video(videoId: 1, title: 'ignore', playlist: playlist, user: user)

        then:
        !duplicate.validate()

        and:
        duplicate.errors.errorCount == 1
        duplicate.errors['videoId'] == 'unique'
    }

    void "title cannot be blank"() {
        given:
        mockForConstraintsTests(Video)

        def user = new User()
        def playlist = new Playlist()

        when:
        def video = new Video(title: '', playlist: playlist, user: user)

        then:
        !video.validate()

        and:
        video.errors.errorCount == 1
        video.errors['title'] == 'nullable'
    }

}