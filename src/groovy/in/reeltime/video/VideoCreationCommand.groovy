package in.reeltime.video

import grails.validation.Validateable
import in.reeltime.user.User

@Validateable
class VideoCreationCommand {
    User creator

    String title
    String description

    InputStream videoStream

    Boolean videoStreamSizeIsValid
    Integer durationInSeconds

    Boolean h264StreamIsPresent
    Boolean aacStreamIsPresent

    static maxDuration

    static constraints = {
        title nullable: false, blank: false
        videoStream nullable: false

        videoStreamSizeIsValid nullable: true, validator: videoStreamSizeIsValidValidator
        durationInSeconds nullable: true, validator: durationInSecondsValidator

        h264StreamIsPresent nullable: true, validator: h264StreamValidator
        aacStreamIsPresent nullable: true, validator: aacStreamValidator
    }

    private static Closure videoStreamSizeIsValidValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->

            if(!val) {
                return 'exceedsMaxSize'
            }
            else if(videoStreamIsNull) {
                return 'videoStreamSizeIsInvalid'
            }
        }
    }

    private static Closure durationInSecondsValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->

            if(exceedsMaxDuration(val)) {
                return 'exceedsMaxDuration'
            }
            else if(videoStreamIsNull) {
                return 'durationIsInvalid'
            }
        }
    }

    private static boolean exceedsMaxDuration(val) {
        val >  maxDuration
    }

    private static Closure h264StreamValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->
            if(!val) {
                return 'h264IsMissing'
            }
            else if(videoStreamIsNull) {
                return 'h264IsInvalid'
            }
        }
    }

    private static Closure aacStreamValidator = { val, obj ->
        videoStreamDependentValidator(val, obj) { videoStreamIsNull ->
            if(!val) {
                return 'aacIsMissing'
            }
            else if(videoStreamIsNull) {
                return 'aacIsInvalid'
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
}
