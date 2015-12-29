package in.reeltime.playlist

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.video.Video

@ToString(includeNames = true)
class PlaylistVideo extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    Playlist playlist
    Video video

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        playlist nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['playlist', 'video']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'playlist'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
