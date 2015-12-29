package in.reeltime.reel

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.video.Video
import org.apache.commons.lang.builder.HashCodeBuilder

@ToString(includeNames = true)
class ReelVideo extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    Reel reel
    Video video

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        reel nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['reel', 'video']
        version false
    }

    static List<Long> findAllVideoIdsByReel(Reel reel) {
        ReelVideo.withCriteria {
            eq('reel', reel)
            projections {
                property('video.id')
            }
        } as List<Long>
    }

    @Override
    String getLeftPropertyName() {
        return 'reel'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
