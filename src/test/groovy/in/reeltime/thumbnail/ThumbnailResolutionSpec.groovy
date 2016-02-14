package in.reeltime.thumbnail

import spock.lang.Specification
import spock.lang.Unroll

class ThumbnailResolutionSpec extends Specification {

    @Unroll
    void "resolution [#resolution] has width [#width] and height [#height]"() {
        expect:
        resolution.width == width
        resolution.height == height

        where:
        resolution                          |   width   |   height
        ThumbnailResolution.RESOLUTION_1X   |   75      |   75
        ThumbnailResolution.RESOLUTION_2X   |   150     |   150
        ThumbnailResolution.RESOLUTION_3X   |   225     |   225
    }
}
