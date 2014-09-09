package in.reeltime.oauth2

class AccessToken {

    String authenticationKey
    byte[] authentication

    String username
    String clientId

    String value
    String tokenType

    Date expiration

    static hasOne = [refreshToken: String]
    static hasMany = [scope: String]

    static constraints = {
        username nullable: true
        clientId nullable: false, blank: false
        value nullable: false, blank: false, unique: true
        tokenType nullable: false, blank: false
        expiration nullable: true
        scope nullable: false
        refreshToken nullable: true
        authenticationKey nullable: false, blank: false, unique: true
        authentication nullable: false, maxSize: 1024 * 4, validator: { val, obj -> val.size() > 0 }
    }

    static mapping = {
        version false
        scope lazy: false
    }
}
