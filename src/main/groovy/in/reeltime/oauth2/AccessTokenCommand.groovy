package in.reeltime.oauth2

import grails.validation.Validateable

class AccessTokenCommand implements Validateable {

    String access_token

    static constraints = {
        access_token nullable: false, blank: false
    }
}
