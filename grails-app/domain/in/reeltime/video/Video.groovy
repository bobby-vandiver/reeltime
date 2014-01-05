package in.reeltime.video

import in.reeltime.video.playlist.Playlist

class Video {

    enum ConversionStatus { SUBMITTED }
    ConversionStatus status = ConversionStatus.SUBMITTED

    User creator

    String title
    String description

    static hasMany = [playlists: Playlist]

    static constraints = {
        creator nullable: true
        title blank: false
        description blank: true, nullable: true
    }
}
