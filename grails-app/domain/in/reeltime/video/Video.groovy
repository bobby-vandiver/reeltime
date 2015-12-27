package in.reeltime.video

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistUri
import in.reeltime.playlist.PlaylistVideo
import in.reeltime.playlist.PlaylistUriVideo
import in.reeltime.thumbnail.Thumbnail
import in.reeltime.thumbnail.ThumbnailVideo
import in.reeltime.user.User

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['masterPath'])
class Video {

    String title

    String masterPath
    String masterThumbnailPath

    boolean available
    Date dateCreated

    User creator

    static transients = ['thumbnails', 'playlists', 'playlistUris']

    static constraints = {
        creator nullable: false
        title nullable: false, blank: false
        masterPath nullable: false, blank: false, unique: true
        masterThumbnailPath nullable: false, blank: false, unique: true
    }

    Set<Thumbnail> getThumbnails() {
        ThumbnailVideo.findAllByVideo(this)*.thumbnail
    }

    Set<Playlist> getPlaylists() {
        PlaylistVideo.findAllByVideo(this)*.playlist
    }

    Set<PlaylistUri> getPlaylistUris() {
        PlaylistUriVideo.findAllByVideo(this)*.playlistUri
    }
}
