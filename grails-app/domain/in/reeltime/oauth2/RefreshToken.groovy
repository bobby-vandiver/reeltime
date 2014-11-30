package in.reeltime.oauth2

class RefreshToken {

    String value
    Date expiration
    byte[] authentication

    static constraints = {
        value nullable: false, blank: false, unique: true
        expiration nullable: false
        authentication nullable: false, minSize: 1, maxSize: 1024 * 4
    }

    static mapping = {
        version false
    }
}
