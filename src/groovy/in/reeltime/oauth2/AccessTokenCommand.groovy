package in.reeltime.oauth2

import grails.validation.Validateable

@Validateable
class AccessTokenCommand {

    String access_token

    static constraints = {
        access_token nullable: false, blank: false
    }
}
