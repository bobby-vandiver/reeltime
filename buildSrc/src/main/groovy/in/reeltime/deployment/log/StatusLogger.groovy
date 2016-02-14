package in.reeltime.deployment.log

import java.text.SimpleDateFormat

class StatusLogger {

    static void displayStatus(String status) {
        println "[${timestamp()}] $status"
    }

    private static String timestamp() {
        def dateFormat = new SimpleDateFormat('EEE MMM d yyyy HH:mm:ss')
        dateFormat.format(new Date())
    }
}
