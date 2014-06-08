package in.reeltime.metadata

import grails.validation.Validateable

@Validateable
class StreamMetadata {

    private static final DURATION_FORMAT = /(\d+)\.(\d+)/

    String codecName
    String duration

    Integer getDurationInSeconds() {
        def matcher = (duration =~ DURATION_FORMAT)
        if(!matcher.matches()) {
            return null
        }

        def seconds = matcher[0][1] as int
        def subSeconds = matcher[0][2] as int

        if(subSeconds > 0) {
            seconds++
        }
        return seconds
    }

    static constraints = {
        duration nullable: false, blank: false, matches: DURATION_FORMAT
    }
}
