package in.reeltime.thumbnail

import grails.validation.Validateable

class ThumbnailCommand implements Validateable {

    String resolution

    static constraints = {
        resolution nullable: false, blank: false, inList: ['small', 'medium', 'large']
        thumbnailResolution nullable: true
    }

    private static final Map<String, ThumbnailResolution> thumbnailResolutionLookupTable = [
            small: ThumbnailResolution.RESOLUTION_1X,
            medium: ThumbnailResolution.RESOLUTION_2X,
            large: ThumbnailResolution.RESOLUTION_3X
    ]

    ThumbnailResolution getThumbnailResolution() {
        thumbnailResolutionLookupTable[resolution]
    }
}
