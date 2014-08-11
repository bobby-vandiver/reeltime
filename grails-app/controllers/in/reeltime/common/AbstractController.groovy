package in.reeltime.common

abstract class AbstractController {

    def localizedMessageService

    static final JSON_CONTENT_TYPE = 'application/json'

    void exceptionErrorMessageResponse(Exception e, String messageCode, int statusCode) {
        def exceptionClassName = e.class.simpleName
        log.warn("Handling $exceptionClassName: ", e)

        errorMessageResponse(messageCode, statusCode)
    }

    void errorMessageResponse(String messageCode, int statusCode) {
        def message = localizedMessageService.getMessage(messageCode, request.locale)
        render(status: statusCode, contentType: JSON_CONTENT_TYPE) {
            [errors: [message]]
        }
    }

    void commandErrorMessageResponse(Object command, int statusCode) {
        render(status: statusCode, contentType: JSON_CONTENT_TYPE) {
            [errors: localizedMessageService.getErrorMessages(command, request.locale)]
        }
    }
}
