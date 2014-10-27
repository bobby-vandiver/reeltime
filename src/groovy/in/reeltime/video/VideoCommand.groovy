package in.reeltime.video

import grails.validation.Validateable

@Validateable
class VideoCommand {

    Long videoId

    static constraints = {
        videoId nullable: false
    }
}
