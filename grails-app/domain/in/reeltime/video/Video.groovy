package in.reeltime.video

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.user.User
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['masterPath'])
class Video {

    String title

    String masterPath
    String masterThumbnailPath

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
        masterPath nullable: false, blank: false, unique: true
        masterThumbnailPath nullable: false, blank: false, unique: true
    }
}
