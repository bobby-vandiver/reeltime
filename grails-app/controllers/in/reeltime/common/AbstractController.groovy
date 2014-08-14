package in.reeltime.common

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST

abstract class AbstractController {

    def localizedMessageService

    protected void exceptionErrorMessageResponse(Exception e, String messageCode, int statusCode) {
        def exceptionClassName = e.class.simpleName
        log.warn("Handling $exceptionClassName: ", e)

        errorMessageResponse(messageCode, statusCode)
    }

    protected void errorMessageResponse(String messageCode, int statusCode) {
        def message = localizedMessageService.getMessage(messageCode, request.locale)
        render(status: statusCode, contentType: APPLICATION_JSON) {
            [errors: [message]]
        }
    }

    protected commandErrorMessageResponse(Object command, int statusCode) {
        render(status: statusCode, contentType: APPLICATION_JSON) {
            [errors: localizedMessageService.getErrorMessages(command, request.locale)]
        }
    }

    protected void handleSingleParamRequest(Object paramToCheck, String errorMessageCode, Closure action) {
        paramToCheck ? action() : errorMessageResponse(errorMessageCode, SC_BAD_REQUEST)
    }

}
