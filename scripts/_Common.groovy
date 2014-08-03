import grails.util.Environment

import java.text.SimpleDateFormat

displayStatus = { String status ->
    println "[${timestamp()}] $status"
}

grailsEnvironmentName = {
    return Environment.currentEnvironment.name
}

String timestamp() {
    def dateFormat = new SimpleDateFormat('EEE MMM d yyyy HH:mm:ss')
    dateFormat.format(new Date())
}

waitForCondition = { String statusMessage, String failureMessage, long pollingInterval,  Closure condition ->

    final MAX_RETRIES = 90
    int retryCount = 0

    while(!condition() && retryCount < MAX_RETRIES) {
        displayStatus(statusMessage)
        sleep(pollingInterval)
        retryCount++
    }

    if(!condition() && retryCount >= MAX_RETRIES) {
        displayStatus(failureMessage)
        System.exit(1)
    }
}

loadApplicationProperties = {
    def applicationProperties = new Properties()
    new File('application.properties').withInputStream { input ->
        applicationProperties.load(input)
    }
    return new ConfigSlurper().parse(applicationProperties)
}