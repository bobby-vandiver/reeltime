package in.reeltime.video

import grails.validation.Validateable

class VideoCommand implements Validateable {

    Long video_id

    static constraints = {
        video_id nullable: false
    }
}
