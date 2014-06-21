package in.reeltime.error

import javax.servlet.http.HttpServletResponse

class ErrorsController {

    def methodNotAllowed() {
        render(status: HttpServletResponse.SC_METHOD_NOT_ALLOWED)
    }
}
