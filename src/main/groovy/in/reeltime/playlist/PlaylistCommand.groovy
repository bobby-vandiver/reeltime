package in.reeltime.playlist

import grails.validation.Validateable

class PlaylistCommand implements Validateable {

    Long playlist_id

    static constraints = {
        playlist_id nullable: false
    }
}
