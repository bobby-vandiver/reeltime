package in.reeltime.springsecurity

import grails.plugin.springsecurity.annotation.Secured

/**
 * This overrides the LogoutController provided by Spring Security in order to lock it down.
 */
@Secured("denyAll")
class LogoutController {

    def index() {}
}
