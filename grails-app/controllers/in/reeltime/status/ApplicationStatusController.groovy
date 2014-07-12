package in.reeltime.status

import grails.plugin.springsecurity.annotation.Secured
import javax.servlet.http.HttpServletResponse

class ApplicationStatusController {

    @Secured(["permitAll"])
    def available() {
        render(status: HttpServletResponse.SC_OK)
    }
}
