package in.reeltime.mail.mailgun

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import in.reeltime.mail.Email
import in.reeltime.mail.MailService

class MailgunService implements MailService {

    String apiKey

    String baseUrl
    String domainName

    RestBuilder restClient = new RestBuilder()

    @Override
    void sendMail(Email email) {
        String basicAuthHeader = "api:$apiKey".bytes.encodeBase64()

        RestResponse response = restClient.post(url) {
            contentType "multipart/form-data"
            header "Authorization", "Basic $basicAuthHeader"

            from = email.from
            to = email.to
            subject = email.subject
            text = email.body
        }

        log.debug "Email [${email}] sent -- received status code [${response.status}]"

        if (response.status != 200) {
            log.warn("Failed to send email [$email] -- received status code [${response.status}]")
        }
    }

    private String getUrl() {
        return "$baseUrl/$domainName/messages"
    }
}
