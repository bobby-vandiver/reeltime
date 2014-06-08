package in.reeltime.video

import grails.validation.Validateable
import in.reeltime.metadata.StreamMetadata
import in.reeltime.user.User

@Validateable
class VideoCreationCommand {
    User creator

    String title
    String description

    InputStream videoStream
    List<StreamMetadata> streams

    static constraints = {
        title nullable: false, blank: false
        videoStream nullable: false
        streams validator: streamsValidator
    }

    private static Closure streamsValidator = { val, obj ->
        if(videoStreamAndStreamsAreNull(val, obj)) {
            return true
        }
        containsOnlyValidStreams(val) && containsRequiredStreams(val)
    }

    private static boolean videoStreamAndStreamsAreNull(val, obj) {
        !val && !obj.videoStream
    }

    private static boolean containsOnlyValidStreams(val) {
        val.find { !it.validate() } == null
    }

    private static boolean containsRequiredStreams(val) {
        val.find { it.codecName == 'h264' } && val.find { it.codecName == 'aac' }
    }
}
