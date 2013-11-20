package in.reeltime.video

class Video {

    enum ConversionStatus { SUBMITTED }

    long videoId
    String title

    ConversionStatus status = ConversionStatus.SUBMITTED

    static belongsTo = [user: User]
    static hasOne = [playlist: Playlist]

    static constraints = {
        title blank: false
    }
}
