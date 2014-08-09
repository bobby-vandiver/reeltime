package in.reeltime.common

abstract class AbstractController {

    def localizedMessageService

    static final JSON_CONTENT_TYPE = 'application/json'

    void handleErrorMessageResponse(Exception e, String messageCode, int statusCode) {
        def exceptionClassName = e.class.simpleName
        log.warn("Handling $exceptionClassName: ", e)

        def message = localizedMessageService.getMessage(messageCode, request.locale)
        render(status: statusCode, contentType: 'application/json') {
            [errors: [message]]
        }
    }
}
