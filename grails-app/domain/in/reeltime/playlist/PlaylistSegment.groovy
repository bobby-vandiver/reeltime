package in.reeltime.playlist

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain

@ToString(includeNames = true)
class PlaylistSegment extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    Playlist playlist
    Segment segment

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        playlist nullable: false
        segment nullable: false
    }

    static mapping = {
        id composite: ['playlist', 'segment']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'playlist'
    }

    @Override
    String getRightPropertyName() {
        return 'segment'
    }
}
