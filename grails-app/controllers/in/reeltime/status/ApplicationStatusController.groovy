package in.reeltime.status

import grails.plugin.springsecurity.annotation.Secured
import static javax.servlet.http.HttpServletResponse.*

class ApplicationStatusController {

    @Secured(["permitAll"])
    def available() {
        render(status: SC_OK)
    }
}
