package in.reeltime.video

import grails.validation.Validateable

@Validateable(nullable = true)
class VideoCommand {

    Long video_id

    static constraints = {
        video_id nullable: false
    }
}
