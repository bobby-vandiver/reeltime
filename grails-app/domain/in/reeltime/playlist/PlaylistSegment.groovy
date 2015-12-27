package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['playlist.id', 'segment.id'])
class PlaylistSegment implements Serializable {

    private static final long serialVersionUID = 1

    Playlist playlist
    Segment segment

    static constraints = {
        playlist nullable: false
        segment nullable: false
    }

    static mapping = {
        id composite: ['playlist', 'segment']
        version false
    }
}
