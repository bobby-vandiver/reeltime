package in.reeltime.notification

import com.amazonaws.AmazonClientException
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest
import com.amazonaws.services.sns.util.SignatureChecker
import in.reeltime.exceptions.NotificationException

import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class NotificationService {

    def awsService

    private static final AUTHENTICATE_ON_UNSUBSCRIBE = 'true'

    def verifyMessage(Map<String, String> parsedMessage) {
        log.debug("Verifying parsedMessage [$parsedMessage]")

        def signingCertUrl = parsedMessage.SigningCertURL

        def certificate = retrieveCertificate(signingCertUrl)
        def publicKey = certificate.publicKey

        def checker = new SignatureChecker()
        checker.verifyMessageSignature(parsedMessage, publicKey)
    }

    private X509Certificate retrieveCertificate(String signingCertUrl) {
        def url = new URL(signingCertUrl)
        def inputStream = url.openStream()
        def certificateFactory = CertificateFactory.getInstance('X.509')
        def certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
        inputStream.close()
        return certificate
    }

    def confirmSubscription(String topicArn, String token) {
        log.debug("Confirming subscription for topicArn [$topicArn] and token [$token]")
        try {
            def client = awsService.createClient(AmazonSNS) as AmazonSNS
            def confirmRequest = new ConfirmSubscriptionRequest(topicArn, token, AUTHENTICATE_ON_UNSUBSCRIBE)
            client.confirmSubscription(confirmRequest)
        }
        catch(AmazonClientException e) {
            def message = "Failed to confirm subscription for topicArn [$topicArn] and token [$token]"
            throw new NotificationException(message, e)
        }
    }
}
