package in.reeltime.video

class Video {

    long videoId
    String title

    static belongsTo = [user: User]
    static hasOne = [playlist: Playlist]

    static constraints = {
        videoId unique: true
        title blank: false
    }
}
