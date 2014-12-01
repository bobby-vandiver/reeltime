package in.reeltime.video

import groovy.transform.ToString
import in.reeltime.user.User
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri

@ToString(includeNames = true)
class Video {

    String title
    String masterPath

    boolean available
    Date dateCreated

    static belongsTo = [creator: User]

    static hasMany = [
            playlists: Playlist,
            playlistUris: PlaylistUri
    ]

    static constraints = {
        creator nullable: false
        title nullable: false, blank: false
        masterPath nullable: false, blank: false
    }
}
