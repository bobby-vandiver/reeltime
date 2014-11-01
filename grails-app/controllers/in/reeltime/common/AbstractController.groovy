package in.reeltime.common

import in.reeltime.exceptions.AuthorizationException

import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN

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

    protected void handleCommandRequest(Object command, Closure action) {
        try {
            !command.hasErrors() ? action() : commandErrorMessageResponse(command, SC_BAD_REQUEST)
        }
        catch(AuthorizationException e) {
            exceptionStatusCodeOnlyResponse(e, SC_FORBIDDEN)
        }
    }

    protected void handleMultipleCommandRequest(Collection<Object> commands, Closure action) {
        try {
            List<String> errors = []

            commands.each { command ->
                if (command.hasErrors()) {
                    def commandErrors = localizedMessageService.getErrorMessages(command, request.locale)
                    errors.addAll(commandErrors)
                }
            }

            if (errors.isEmpty()) {
                action()
            } else {
                render(status: SC_BAD_REQUEST, contentType: APPLICATION_JSON) {
                    [errors: errors]
                }
            }
        }
        catch(AuthorizationException e) {
            exceptionStatusCodeOnlyResponse(e, SC_FORBIDDEN)
        }
    }
}
