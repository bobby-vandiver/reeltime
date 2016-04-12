package in.reeltime.mail

import org.xbill.DNS.Lookup
import org.xbill.DNS.MXRecord
import org.xbill.DNS.Record
import org.xbill.DNS.Type

class MailServerService {

    boolean exists(String host) {
        boolean exists = false

        try {
            Record[] records = new Lookup(host, Type.MX).run()
            exists = records?.length > 0

            records.each { MXRecord record ->
                log.debug "Found MXRecord: [$record]"
            }
        }
        catch (Exception e) {
            log.debug "Failed to lookup MX records for host [$host]", e
        }

        return exists
    }
}
