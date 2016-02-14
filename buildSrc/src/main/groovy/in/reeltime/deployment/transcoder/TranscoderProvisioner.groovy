package in.reeltime.deployment.transcoder

import com.amazonaws.services.elastictranscoder.model.CreatePipelineRequest
import com.amazonaws.services.elastictranscoder.model.Notifications
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.sns.model.CreateTopicResult
import in.reeltime.deployment.aws.client.EnhancedAmazonElasticTranscoder
import in.reeltime.deployment.aws.client.EnhancedAmazonIdentityManagement
import in.reeltime.deployment.aws.client.EnhancedAmazonS3
import in.reeltime.deployment.aws.client.EnhancedAmazonSNS
import in.reeltime.deployment.configuration.DeploymentConfiguration
import static in.reeltime.deployment.log.StatusLogger.*

class TranscoderProvisioner {

    String transcoderTopicArn

    private DeploymentConfiguration deployConfig

    private EnhancedAmazonS3 s3
    private EnhancedAmazonSNS sns
    private EnhancedAmazonElasticTranscoder ets
    private EnhancedAmazonIdentityManagement iam

    TranscoderProvisioner(DeploymentConfiguration deployConfig, EnhancedAmazonS3 s3, EnhancedAmazonSNS sns,
                          EnhancedAmazonElasticTranscoder ets, EnhancedAmazonIdentityManagement iam) {
        this.deployConfig = deployConfig
        this.s3 = s3
        this.sns = sns
        this.ets = ets
        this.iam = iam
    }

    void configureTranscoder() {
        createTranscoderNotificationTopic()
        createPipeline()
    }

    void createTranscoderNotificationTopic() {

        String topicName = deployConfig.configuration.transcoder.topicName
        String topicArn = null

        if(deployConfig.resetResourcesIsAllowed()) {
            displayStatus("Fetching subscription ARNs for all subscriptions for topic [$topicName]")
            Collection<String> subscriptionArns = sns.findSubscriptionArnsByTopicName(topicName)

            subscriptionArns.each { subscriptionArn ->
                displayStatus("Unsubscribing [$subscriptionArn]")
                sns.unsubscribe(subscriptionArn)
            }
        }

        if(sns.topicExists(topicName)) {
            displayStatus("Topic [$topicName] already exists -- fetching topic ARN")
            topicArn = sns.findTopicArnByName(topicName)

            if(deployConfig.resetResourcesIsAllowed()) {
                displayStatus("Deleting existing topic [$topicName]")
                sns.deleteTopic(topicArn)
                topicArn = null
            }
        }

        if(!sns.topicExists(topicName)) {
            displayStatus("Creating notification topic [$topicName]")
            CreateTopicResult result = sns.createTopic(topicName)
            topicArn = result.topicArn
        }

        transcoderTopicArn = topicArn
    }

    void createPipeline() {

        String pipelineName = deployConfig.configuration.transcoder.pipelineName

        if(ets.pipelineExists(pipelineName)) {
            displayStatus("Pipeline [$pipelineName] already exists -- nothing to configure")
            return
        }

        String roleName = deployConfig.configuration.transcoder.roleName

        if(!iam.roleExists(roleName)) {
            displayStatus("Transcoder role [$roleName] must exist!")
            System.exit(1)
        }

        String roleArn = iam.findRoleArnByName(roleName)

        String inputBucket = deployConfig.configuration.transcoder.inputBucket
        String outputBucket = deployConfig.configuration.transcoder.outputBucket

        ensureBucketIsAvailable(inputBucket)
        ensureBucketIsAvailable(outputBucket)

        Notifications notifications = new Notifications(
                completed: transcoderTopicArn,
                error: transcoderTopicArn,
                progressing: transcoderTopicArn,
                warning: transcoderTopicArn
        )

        CreatePipelineRequest createPipelineRequest = new CreatePipelineRequest(
                name: pipelineName,
                role: roleArn,
                inputBucket: inputBucket,
                outputBucket: outputBucket,
                notifications: notifications
        )

        displayStatus("Creating pipeline: $createPipelineRequest")
        ets.createPipeline(createPipelineRequest)
    }

    private void ensureBucketIsAvailable(String bucketName) {

        if(s3.bucketExists(bucketName)) {
            if(deployConfig.resetResourcesIsAllowed()) {

                displayStatus("Emptying existing bucket [$bucketName]")
                s3.listObjects(bucketName).objectSummaries.each { S3ObjectSummary obj ->
                    s3.deleteObject(bucketName, obj.key)
                }

                displayStatus("Deleting existing bucket [$bucketName]")
                s3.deleteBucket(bucketName)
            }
            else {
                throw new IllegalStateException("Bucket [$bucketName] exists and removal is not permitted")
            }
        }

        if(!s3.bucketExists(bucketName)) {
            displayStatus("Creating bucket [$bucketName]")
            s3.createBucket(bucketName)
        }
    }
}
