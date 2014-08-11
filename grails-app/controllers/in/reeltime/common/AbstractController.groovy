package in.reeltime.common

import static in.reeltime.common.ContentTypes.APPLICATION_JSON

abstract class AbstractController {

    def localizedMessageService

    void exceptionErrorMessageResponse(Exception e, String messageCode, int statusCode) {
        def exceptionClassName = e.class.simpleName
        log.warn("Handling $exceptionClassName: ", e)

        errorMessageResponse(messageCode, statusCode)
    }

    void errorMessageResponse(String messageCode, int statusCode) {
        def message = localizedMessageService.getMessage(messageCode, request.locale)
        render(status: statusCode, contentType: APPLICATION_JSON) {
            [errors: [message]]
        }
    }

    void commandErrorMessageResponse(Object command, int statusCode) {
        render(status: statusCode, contentType: APPLICATION_JSON) {
            [errors: localizedMessageService.getErrorMessages(command, request.locale)]
        }
    }
}
