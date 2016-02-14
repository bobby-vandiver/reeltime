package in.reeltime.video

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class VideoIntegrationSpec extends Specification {

    void "masterPath must be unique"() {
        given:
        new Video(title: 'existing', masterThumbnailPath: 't1', masterPath: 'path').save()

        when:
        def video = new Video(title: 'new', masterThumbnailPath: 't2', masterPath: 'path')

        then:
        !video.validate(['masterPath'])
    }

    void "masterThumbnailPath must be unique"() {
        given:
        new Video(title: 'existing', masterThumbnailPath: 'thumbnail', masterPath: 'p1').save()

        when:
        def video = new Video(title: 'new', masterThumbnailPath: 'thumbnail', masterPath: 'p2')

        then:
        !video.validate(['masterThumbnailPath'])
    }
}
