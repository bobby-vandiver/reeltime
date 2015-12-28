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

    static transients = ['creator', 'thumbnails', 'playlists', 'playlistUris']

    static constraints = {
        title nullable: false, blank: false
        masterPath nullable: false, blank: false, unique: true
        masterThumbnailPath nullable: false, blank: false, unique: true
    }

    User getCreator() {
        VideoCreator.findByVideo(this)?.creator
    }

    Collection<Thumbnail> getThumbnails() {
        ThumbnailVideo.findAllByVideo(this)*.thumbnail
    }

    Collection<Playlist> getPlaylists() {
        PlaylistVideo.findAllByVideo(this)*.playlist
    }

    Collection<PlaylistUri> getPlaylistUris() {
        PlaylistUriVideo.findAllByVideo(this)*.playlistUri
    }
}
