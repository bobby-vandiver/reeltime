package in.reeltime.video

import in.reeltime.video.playlist.Playlist

class Video {

    enum ConversionStatus { SUBMITTED }

    long videoId
    String title

    ConversionStatus status = ConversionStatus.SUBMITTED

    static belongsTo = [user: User]
    static hasMany = [playlists: Playlist]

    static constraints = {
        title blank: false
    }
}
