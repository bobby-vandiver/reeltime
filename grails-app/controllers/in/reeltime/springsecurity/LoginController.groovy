package in.reeltime.springsecurity

import grails.plugin.springsecurity.annotation.Secured

/**
 * This overrides the LoginController provided by Spring Security in order to lock it down.
 */
@Secured("denyAll")
class LoginController {

    def index() { }
}
