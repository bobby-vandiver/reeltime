package in.reeltime.error

import javax.servlet.http.HttpServletResponse

class ErrorsController {

    def methodNotAllowed() {
        render(status: HttpServletResponse.SC_METHOD_NOT_ALLOWED)
    }

    def internalServerError() {
        logExceptionIfPresent()
        render(status: HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    }

    private void logExceptionIfPresent() {
        def e = request.exception
        if(e) {
            log.error("Exception caught:", e)
        }
    }
}
