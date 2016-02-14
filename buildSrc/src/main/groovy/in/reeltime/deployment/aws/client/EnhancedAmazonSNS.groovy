package in.reeltime.deployment.aws.client

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.Subscription
import com.amazonaws.services.sns.model.Topic

class EnhancedAmazonSNS implements AmazonSNS {

    @Delegate
    AmazonSNS amazonSNS

    EnhancedAmazonSNS(AmazonSNS amazonSNS) {
        this.amazonSNS = amazonSNS
    }

    boolean topicExists(String topicName) {
        return findTopicByName(topicName) != null
    }

    String findTopicArnByName(String topicName) {
        return findTopicByName(topicName)?.topicArn
    }

    List<String> findSubscriptionArnsByTopicName(String topicName) {
        return findSubscriptionsByTopicName(topicName)*.subscriptionArn
    }

    private Topic findTopicByName(String topicName) {
        return listTopics().topics.find { it.topicArn.endsWith(topicName) }
    }

    private List<Subscription> findSubscriptionsByTopicName(String topicName) {
        return listSubscriptions().subscriptions.findAll { it.topicArn.endsWith(topicName) }
    }
}
