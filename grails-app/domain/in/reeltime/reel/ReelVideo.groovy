package in.reeltime.reel

import in.reeltime.video.Video
import org.apache.commons.lang.builder.HashCodeBuilder

class ReelVideo implements Serializable {

    private static final long serialVersionUID = 1

    Reel reel
    Video video

    static constraints = {
        reel nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['reel', 'video']
        version false
    }

    @Override
    int hashCode() {
        def builder = new HashCodeBuilder()
        if(reel) builder.append(reel.id)
        if(video) builder.append(video.id)
        builder.toHashCode()
    }

    @Override
    boolean equals(Object other) {
        if(!(other instanceof ReelVideo)) {
            return false
        }
        boolean sameReel = (other.reel?.id == reel?.id)
        boolean sameVideo = (other.video?.id == video?.id)

        return sameReel && sameVideo
    }
}
