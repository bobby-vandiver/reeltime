package in.reeltime.message

import grails.transaction.Transactional

@Transactional
class LocalizedMessageService {

    def messageSource

    String getMessage(code, locale, args = []) {
        messageSource.getMessage(code, args as Object[], locale)
    }

    List<String> getErrorMessages(command, locale) {
        command.errors.allErrors.collect { error ->
            messageSource.getMessage(error, locale)
        }
    }
}
