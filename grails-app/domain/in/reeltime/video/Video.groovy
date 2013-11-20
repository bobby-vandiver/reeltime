package in.reeltime.video

class Video {

    long videoId
    String title

    static belongsTo = [user: User]
    static hasOne = [playlist: Playlist]

    static constraints = {
        title blank: false
    }
}
