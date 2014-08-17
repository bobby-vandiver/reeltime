package in.reeltime.video

import in.reeltime.user.User
import in.reeltime.playlist.Playlist

class Video {

    String title
    String description

    String masterPath
    boolean available

    static belongsTo = [creator: User]
    static hasMany = [playlists: Playlist]

    static constraints = {
        creator nullable: true
        title blank: false
        description blank: true, nullable: true
        masterPath blank: false
    }
}
