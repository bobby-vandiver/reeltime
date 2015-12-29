package in.reeltime.thumbnail

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.video.Video

@ToString(includeNames = true)
class ThumbnailVideo extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    Thumbnail thumbnail
    Video video

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        thumbnail nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['thumbnail', 'video']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'thumbnail'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
