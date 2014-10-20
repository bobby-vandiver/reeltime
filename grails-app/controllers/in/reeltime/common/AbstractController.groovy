package in.reeltime.common

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST

abstract class AbstractController {

    def localizedMessageService

    @Delegate
    final CustomMarshaller customMarshaller = new CustomMarshaller()

    protected void exceptionErrorMessageResponse(Exception e, String messageCode, int statusCode) {
        logException(e)
        errorMessageResponse(messageCode, statusCode)
    }

    protected void exceptionStatusCodeOnlyResponse(Exception e, int statusCode) {
        logException(e)
        render(status: statusCode)
    }

    private void logException(Exception e) {
        def exceptionClassName = e.class.simpleName
        log.warn("Handling $exceptionClassName: ", e)
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

    protected void handleCommandRequest(Object command, Closure action) {
        !command.hasErrors() ? action() : commandErrorMessageResponse(command, SC_BAD_REQUEST)
    }
}
