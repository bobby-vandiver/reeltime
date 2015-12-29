package in.reeltime.video

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.user.User

@ToString(includeNames = true)
class VideoCreator extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    Video video
    User creator

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        video nullable: false
        creator nullable: false
    }

    static mapping = {
        id composite: ['video', 'creator']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'video'
    }

    @Override
    String getRightPropertyName() {
        return 'creator'
    }
}
