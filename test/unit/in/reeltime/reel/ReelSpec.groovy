package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.user.User
import in.reeltime.video.Video
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Reel)
class ReelSpec extends Specification {

    void "a reel without an owner is impossible"() {
        given:
        def reel = new Reel()

        expect:
        !reel.validate(['owner'])
    }

    void "a reel must have an owner"() {
        given:
        def reel = new Reel(owner: new User())

        expect:
        reel.validate(['owner'])
    }

    void "a reel with no audience is impossible"() {
        given:
        def reel = new Reel()

        expect:
        !reel.validate(['audience'])
    }

    void "a reel must have one audience"() {
        given:
        def reel = new Reel(audience: new Audience())

        expect:
        reel.validate(['audience'])
    }

    void "videos cannot be null"() {
        given:
        def reel = new Reel(videos: null)

        expect:
        !reel.validate(['videos'])
    }

    @Unroll
    void "[#count] videos is valid"() {
        given:
        def videos = createVideos(count)
        def reel = new Reel(videos: videos)

        expect:
        reel.validate(['videos'])

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   10
        _   |   100
        _   |   500
    }

    @Unroll
    void "name [#name] is valid [#valid]"() {
        given:
        def reel = new Reel(name: name)

        expect:
        reel.validate(['name']) == valid

        where:
        name        |   valid
        null        |   false
        ''          |   false
        'a'         |   false
        'a' * 4     |   false
        'a' * 5     |   true
        'a' * 25    |   true
        'a' * 26    |   false
    }

    private static Collection<Video> createVideos(int count) {
        def videos = []
        count.times { videos << new Video() }
        return videos
    }
}
