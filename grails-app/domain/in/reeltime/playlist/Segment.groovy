package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['uri'])
class Segment implements Comparable {

    int segmentId
    String uri
    String duration

    static belongsTo = [playlist: Playlist]

    static constraints = {
        segmentId min: 0
        uri blank: false, nullable: false, unique: true
        duration blank: false, nullable: false,  matches: /^\d+(.\d+)?/
    }

    @Override
    int compareTo(obj) {
        segmentId <=> obj.segmentId
    }
}
