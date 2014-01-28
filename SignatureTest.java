package br.com.ingenieux.services.web;

import java.net.URL;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.sns.util.SignatureChecker;

public class SignatureTest {
	ObjectMapper objectMapper;
	
	ObjectNode messageObj;

	String originalMessage;
	
	@Before
	public void before() throws Exception {
		objectMapper = new ObjectMapper();
		
		this.originalMessage = "{ \"Type\" : \"Notification\", \"MessageId\" : \"ee945a43-582c-55c9-a360-132e334f24b4\", \"TopicArn\" : \"arn:aws:sns:us-east-1:235368163414:idle-dynamodb-table\",  \"Subject\" : \"Test Message\",  \"Message\" : \"Hello, World!\",  \"Timestamp\" : \"2013-01-14T02:10:43.261Z\", \"SignatureVersion\" : \"1\", \"Signature\" : \"QhrMr8wxpxPi2izCevjihxJJw11Zn5CFmDvdV7Qp+J/IOAPdSuYrmencSGk9eVbPlZLG+OEZKnjp5G3udYCysFzsPqauwsAF3gmdKbxn4VaN4F8KMNi1Sw+fIbfAwj+MyPXh3reZV/R/rIuY2yBKXaym04bNujUxrMspKLBWhFI=\", \"SigningCertURL\" : \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\", \"UnsubscribeURL\" : \"https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:235368163414:idle-dynamodb-table:0c7c26fc-726e-4986-a132-276754757cbc\"}";

		this.messageObj = (ObjectNode) objectMapper.readTree(originalMessage);
	}

	@Test
	public void testSignature() throws Exception {
		SignatureChecker sigChecker = new SignatureChecker();
		
		String signature = messageObj.get("Signature").getTextValue();
		URL signingCertURL = new URL(messageObj.get("SigningCertURL").getTextValue());
		
		Certificate x509Cert = CertificateFactory.getInstance("X.509").generateCertificate(signingCertURL.openStream());
		
		PublicKey publicKey = x509Cert.getPublicKey();

		sigChecker.verifySignature(originalMessage, signature, publicKey);
	}
}
