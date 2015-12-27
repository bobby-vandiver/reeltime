package in.reeltime.thumbnail

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.video.Video

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['thumbnail.id', 'video.id'])
class ThumbnailVideo implements Serializable {

    private static final long serialVersionUID = 1

    Thumbnail thumbnail
    Video video

    static constraints = {
        thumbnail nullable: false
        video nullable: false
    }

    static mapping = {
        id composite: ['thumbnail', 'video']
        version false
    }
}
