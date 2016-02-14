package in.reeltime.plugin

import in.reeltime.deployment.aws.AWSClientFactory
import in.reeltime.deployment.aws.client.EnhancedAWSElasticBeanstalk
import in.reeltime.deployment.aws.client.EnhancedAmazonEC2
import in.reeltime.deployment.aws.client.EnhancedAmazonElasticTranscoder
import in.reeltime.deployment.aws.client.EnhancedAmazonIdentityManagement
import in.reeltime.deployment.aws.client.EnhancedAmazonS3
import in.reeltime.deployment.aws.client.EnhancedAmazonSNS
import in.reeltime.deployment.configuration.AwsBeanstalkEnvironmentConfiguration
import in.reeltime.deployment.configuration.DeploymentConfiguration
import in.reeltime.deployment.configuration.GrailsEnvironment
import in.reeltime.deployment.transcoder.TranscoderProvisioner
import in.reeltime.deployment.war.WarDeployer
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeploymentPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            war {
                from("${projectDir}/.ebextensions") {
                    into ".ebextensions"
                }

                from("${projectDir}/external") {
                    include "ffprobe"
                    into "external"
                }
            }

            task(dependsOn: assemble, 'deploy') {
                description = 'Deploys the application to AWS.'
                group = 'build'

                doLast {
                    AWSClientFactory clientFactory = new AWSClientFactory()

                    EnhancedAmazonS3 s3 = clientFactory.createS3Client()
                    EnhancedAmazonIdentityManagement iam = clientFactory.createIAMClient()

                    EnhancedAmazonEC2 ec2 = clientFactory.createEC2Client()
                    EnhancedAWSElasticBeanstalk eb = clientFactory.createEBClient()

                    EnhancedAmazonElasticTranscoder ets = clientFactory.createETSClient()
                    EnhancedAmazonSNS sns = clientFactory.createSNSClient()

                    String environment = GrailsEnvironment.grailsEnv
                    DeploymentConfiguration deployConfig = new DeploymentConfiguration(environment)

                    TranscoderProvisioner transcoderProvisioner = new TranscoderProvisioner(deployConfig, s3, sns, ets, iam)

                    AwsBeanstalkEnvironmentConfiguration beanstalkConfig = new AwsBeanstalkEnvironmentConfiguration(deployConfig, ec2, iam)
                    WarDeployer warDeployer = new WarDeployer(project, deployConfig, beanstalkConfig, transcoderProvisioner, ec2, sns, s3, eb)

                    transcoderProvisioner.configureTranscoder()
                    warDeployer.deployWar()
                }
            }
        }
    }
}
