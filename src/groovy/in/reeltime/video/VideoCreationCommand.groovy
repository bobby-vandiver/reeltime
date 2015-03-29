package in.reeltime.video

import grails.validation.Validateable
import in.reeltime.user.User

@Validateable
class VideoCreationCommand {

    User creator

    String reel
    String title

    InputStream thumbnailStream
    InputStream videoStream

    Boolean thumbnailStreamSizeIsValid
    Boolean thumbnailFormatIsValid

    Boolean videoStreamSizeIsValid
    Integer durationInSeconds

    Boolean h264StreamIsPresent
    Boolean aacStreamIsPresent

    static maxDuration

    static constraints = {
        creator nullable: false

        reel nullable: false, blank: false, validator: reelNameValidator
        title nullable: false, blank: false

        thumbnailStream nullable: false
        videoStream nullable: false

        thumbnailStreamSizeIsValid nullable: true, validator: thumbnailStreamSizeIsValidValidator
        thumbnailFormatIsValid nullable: true, validator: thumbnailFormatIsValidValidator

        videoStreamSizeIsValid nullable: true, validator: videoStreamSizeIsValidValidator
        durationInSeconds nullable: true, validator: durationInSecondsValidator

        h264StreamIsPresent nullable: true, validator: h264StreamValidator
        aacStreamIsPresent nullable: true, validator: aacStreamValidator
    }

    void setVideoStreamSizeValidity(boolean valid) {
        videoStreamSizeIsValid = videoStream ? valid : null
    }

    void setThumbnailStreamSizeValidity(boolean valid) {
        thumbnailStreamSizeIsValid = thumbnailStream ? valid : null
    }

    private static Closure reelNameValidator = { val, obj ->
        if(obj.creator && !obj.creator.hasReel(val)) {
            return 'unknown'
        }
    }

    private static Closure thumbnailStreamSizeIsValidValidator = { val, obj ->
        thumbnailStreamDependentValidator(val, obj) { thumbnailStreamIsNull ->
            if(!val) {
                return 'exceedsMax'
            }
            else if(thumbnailStreamIsNull) {
                return 'invalid'
            }
        }
    }

    private static Closure thumbnailFormatIsValidValidator = { val, obj ->
        thumbnailStreamDependentValidator(val, obj) { thumbnailStreamIsNull ->
            if(!val) {
                return 'format'
            }
            else if(thumbnailStreamIsNull) {
                return 'invalid'
            }
        }
    }

    private static Closure videoStreamSizeIsValidValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->
            if(!val) {
                return 'exceedsMax'
            }
            else if(videoStreamIsNull) {
                return 'invalid'
            }
        }
    }

    private static Closure durationInSecondsValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->
            if(exceedsMaxDuration(val)) {
                return 'exceedsMax'
            }
            else if(videoStreamIsNull) {
                return 'invalid'
            }
        }
    }

    private static boolean exceedsMaxDuration(val) {
        val >  maxDuration
    }

    private static Closure h264StreamValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->
            if(!val) {
                return 'missing'
            }
            else if(videoStreamIsNull) {
                return 'invalid'
            }
        }
    }

    private static Closure aacStreamValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->
            if(!val) {
                return 'missing'
            }
            else if(videoStreamIsNull) {
                return 'invalid'
            }
        }
    }

    private static videoStreamDependentValidator(val, obj, Closure additionalValidation) {
        boolean videoStreamIsNull = obj.videoStream.is(null)
        boolean valueIsNull = val.is(null)

        if(valueIsNull && videoStreamIsNull) {
            return true
        }
        additionalValidation(videoStreamIsNull)
    }

    private static thumbnailStreamDependentValidator(val, obj, Closure additionalValidation) {
        boolean thumbnailStreamIsNull = obj.thumbnailStream.is(null)
        boolean valueIsNull = val.is(null)

        if(valueIsNull && thumbnailStreamIsNull) {
            return true
        }
        additionalValidation(thumbnailStreamIsNull)
    }
}
