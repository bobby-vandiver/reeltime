package in.reeltime.playlist

import grails.validation.Validateable

@Validateable(nullable = true)
class PlaylistCommand {

    Long playlist_id

    static constraints = {
        playlist_id nullable: false
    }
}
