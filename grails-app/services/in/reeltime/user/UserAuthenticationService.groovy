package in.reeltime.user

import grails.plugin.springsecurity.userdetails.GrailsUser

class UserAuthenticationService {

    def springSecurityService

    User getLoggedInUser() {
        def principal = springSecurityService.authentication?.principal as GrailsUser
        principal ? User.findByUsername(principal.username) : null
    }
}
