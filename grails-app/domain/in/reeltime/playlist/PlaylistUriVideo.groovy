package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.video.Video

@ToString
class PlaylistUriVideo extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    PlaylistUri playlistUri
    Video video

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        playlistUri nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['playlistUri', 'video']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'playlistUri'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
