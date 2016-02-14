package in.reeltime.deployment.condition

import static in.reeltime.deployment.log.StatusLogger.displayStatus

class ConditionalWait {

    private static final int MAX_RETRIES = 90

    static void waitForCondition(String statusMessage, String failureMessage, long pollingInterval, Closure condition) {
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
}
