package in.reeltime.metadata

import grails.validation.Validateable

@Validateable
class StreamMetadata {

    private static final DURATION_FORMAT = /(\d+)\.(\d+)/

    String codecName
    String duration

    static maxDuration

    static constraints = {
        duration validator: durationValidator
    }

    private static Closure durationValidator = { val, obj ->
        !invalidDurationFormat(val) && !exceedsMaxDuration(val)
    }

    private static boolean invalidDurationFormat(String duration) {
        !(duration ==~ DURATION_FORMAT)
    }

    private static boolean exceedsMaxDuration(String duration) {
        def matcher = (duration =~ DURATION_FORMAT)

        def seconds = matcher[0][1] as int
        return seconds >= maxDuration
    }

}
