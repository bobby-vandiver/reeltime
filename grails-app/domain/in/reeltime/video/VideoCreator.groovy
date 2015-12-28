package in.reeltime.video

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['video.id', 'creator.username'])
class VideoCreator implements Serializable {

    private static final long serialVersionUID = 1

    Video video
    User creator

    static constraints = {
        video nullable: false
        creator nullable: false
    }

    static mapping = {
        id composite: ['video', 'creator']
        version false
    }
}
