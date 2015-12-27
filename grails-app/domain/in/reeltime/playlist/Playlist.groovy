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

    static transients = ['length', 'segments', 'video']

    static constraints = {
        codecs nullable: true
        resolution nullable: true
    }

    Video getVideo() {
        PlaylistVideo.findByPlaylist(this).video
    }

    List<Segment> getSegments() {
        PlaylistSegment.findAllByPlaylist(this)*.segment.sort()
    }

    int getLength() {
        segments.size()
    }
}
