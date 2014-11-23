import com.amazonaws.services.elastictranscoder.model.CreatePipelineRequest
import com.amazonaws.services.elastictranscoder.model.Notifications
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.sns.model.CreateTopicResult

includeTargets << new File("${basedir}/scripts/_DeployConfig.groovy")
includeTargets << new File("${basedir}/scripts/_Common.groovy")
includeTargets << new File("${basedir}/scripts/_AwsClients.groovy")

target(configureTranscoder: "Configures ETS and SNS for transcoding videos") {
    depends(loadDeployConfig, initAwsClients)

    transcoderTopicArn = createTranscoderNotificationTopic()
    createPipeline()
}

String createTranscoderNotificationTopic() {

    String topicName = deployConfig.transcoder.topicName
    String topicArn = null

    if(resetResourcesIsAllowed()) {
        displayStatus("Fetchin subscription ARNs for all subscriptions for topic [$topicName]")
        Collection<String> subscriptionArns = sns.findSubscriptionArnsByTopicName(topicName)

        subscriptionArns.each { subscriptionArn ->
            displayStatus("Unsubscribing [$subscriptionArn]")
            sns.unsubscribe(subscriptionArn)
        }
    }

    if(sns.topicExists(topicName)) {
        displayStatus("Topic [$topicName] already exists -- fetching topic ARN")
        topicArn = sns.findTopicArnByName(topicName)

        if(resetResourcesIsAllowed()) {
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

    return topicArn
}

void createPipeline() {

    String pipelineName = deployConfig.transcoder.pipelineName

    if(ets.pipelineExists(pipelineName)) {
        displayStatus("Pipeline [$pipelineName] already exists -- nothing to configure")
        return
    }

    String roleName = deployConfig.transcoder.roleName

    if(!iam.roleExists(roleName)) {
        displayStatus("Transcoder role [$roleName] must exist!")
        System.exit(1)
    }

    String roleArn = iam.findRoleArnByName(roleName)

    String inputBucket = deployConfig.transcoder.inputBucket
    String outputBucket = deployConfig.transcoder.outputBucket

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

void ensureBucketIsAvailable(String bucketName) {

    if(s3.bucketExists(bucketName)) {
        if(resetResourcesIsAllowed()) {

            displayStatus("Emptying existing bucket [$bucketName]")
            s3.listObjects(bucketName).objectSummaries.each { S3ObjectSummary obj ->
                s3.deleteObject(bucketName, obj.key)
            }

            displayStatus("Deleting existing bucket [$bucketName]")
            s3.deleteBucket(bucketName)
        }
        else {
            displayStatus("Bucket [$bucketName] exists and removal is not permitted")
            System.exit(1)
        }
    }

    if(!s3.bucketExists(bucketName)) {
        displayStatus("Creating bucket [$bucketName]")
        s3.createBucket(bucketName)
    }
}