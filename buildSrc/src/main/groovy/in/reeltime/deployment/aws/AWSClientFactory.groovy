package in.reeltime.deployment.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sns.AmazonSNSClient
import in.reeltime.deployment.aws.client.EnhancedAWSElasticBeanstalk
import in.reeltime.deployment.aws.client.EnhancedAmazonEC2
import in.reeltime.deployment.aws.client.EnhancedAmazonElasticTranscoder
import in.reeltime.deployment.aws.client.EnhancedAmazonIdentityManagement
import in.reeltime.deployment.aws.client.EnhancedAmazonS3
import in.reeltime.deployment.aws.client.EnhancedAmazonSNS

class AWSClientFactory {

    private AWSCredentials credentials

    AWSClientFactory() {
        this(loadAWSCredentials())
    }

    AWSClientFactory(AWSCredentials credentials) {
        this.credentials = credentials
    }

    EnhancedAmazonS3 createS3Client() {
        def s3 = new AmazonS3Client(credentials)
        return new EnhancedAmazonS3(s3)
    }

    EnhancedAmazonEC2 createEC2Client() {
        def ec2 = new AmazonEC2Client(credentials)
        return new EnhancedAmazonEC2(ec2)
    }

    EnhancedAWSElasticBeanstalk createEBClient() {
        def eb = new AWSElasticBeanstalkClient(credentials)
        return new EnhancedAWSElasticBeanstalk(eb)
    }

    EnhancedAmazonElasticTranscoder createETSClient() {
        def ets = new AmazonElasticTranscoderClient(credentials)
        return new EnhancedAmazonElasticTranscoder(ets)
    }

    EnhancedAmazonSNS createSNSClient() {
        def sns = new AmazonSNSClient(credentials)
        return new EnhancedAmazonSNS(sns)
    }

    EnhancedAmazonIdentityManagement createIAMClient() {
        def iam  = new AmazonIdentityManagementClient(credentials)
        return new EnhancedAmazonIdentityManagement(iam)
    }

    private static AWSCredentials loadAWSCredentials() {
        String accessKey = getSystemProperty('AWSAccessKey')
        String secretKey = getSystemProperty('AWSSecretKey')
        new BasicAWSCredentials(accessKey, secretKey)
    }

    private static String getSystemProperty(String propertyName) {
        String property = System.properties[propertyName]
        if (!property) {
            throw new IllegalArgumentException("System property [$propertyName] is required")
        }
        return property
    }
}
