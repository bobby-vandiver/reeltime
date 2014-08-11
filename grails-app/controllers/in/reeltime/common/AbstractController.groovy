package in.reeltime.common

abstract class AbstractController {

    def localizedMessageService

    static final JSON_CONTENT_TYPE = 'application/json'

    void handleExceptionErrorMessageResponse(Exception e, String messageCode, int statusCode) {
        def exceptionClassName = e.class.simpleName
        log.warn("Handling $exceptionClassName: ", e)

        handleErrorMessageResponse(messageCode, statusCode)
    }

    void handleErrorMessageResponse(String messageCode, int statusCode) {
        def message = localizedMessageService.getMessage(messageCode, request.locale)
        render(status: statusCode, contentType: JSON_CONTENT_TYPE) {
            [errors: [message]]
        }
    }
}
