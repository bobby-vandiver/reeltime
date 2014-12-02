package in.reeltime.playlist

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.video.Video

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['programId', 'bandwidth', 'codecs', 'resolution', 'hlsVersion', 'mediaSequence', 'targetDuration', 'video'])
class Playlist {

    int programId
    int bandwidth

    String codecs
    String resolution

    int hlsVersion
    int mediaSequence
    int targetDuration

    SortedSet<Segment> segments

    static belongsTo = [video: Video]
    static hasMany = [segments: Segment]

    int getLength() {
        segments.size()
    }

    static transients = ['length']

    static constraints = {
        codecs nullable: true
        resolution nullable: true
    }
}
