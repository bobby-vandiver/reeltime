import grails.util.BuildSettings
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

root(ERROR, ['STDOUT'])

logger 'in.reeltime', DEBUG

logger 'grails.app.controllers', DEBUG
logger 'grails.app.services', DEBUG
logger 'grails.app.domain', DEBUG
logger 'grails.app.filters', DEBUG
logger 'grails.app.conf', DEBUG
logger 'grails.app.taglib', DEBUG

logger 'grails.plugin.springsecurity', DEBUG
logger 'org.springframework.security', DEBUG

if(System.getProperty('ENABLE_SQL_LOGGING') == 'true') {
    logger 'org.hibernate.type', TRACE
    logger 'org.hibernate.SQL', DEBUG
}

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
