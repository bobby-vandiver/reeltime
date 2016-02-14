package in.reeltime.deployment.aws.client

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.Pipeline

class EnhancedAmazonElasticTranscoder implements AmazonElasticTranscoder {

    @Delegate
    AmazonElasticTranscoder amazonElasticTranscoder

    EnhancedAmazonElasticTranscoder(AmazonElasticTranscoder amazonElasticTranscoder) {
        this.amazonElasticTranscoder = amazonElasticTranscoder
    }

    boolean pipelineExists(String pipelineName) {
        return findPipelineByName(pipelineName) != null
    }

    private Pipeline findPipelineByName(String pipelineName) {
        return listPipelines().pipelines.find { it.name == pipelineName }
    }
}
