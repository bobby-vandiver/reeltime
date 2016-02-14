package in.reeltime.deployment.aws.client

import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.Reservation
import com.amazonaws.services.ec2.model.SecurityGroup
import com.amazonaws.services.ec2.model.Subnet

class EnhancedAmazonEC2 implements AmazonEC2 {

    private static final String NAME_TAG_KEY = 'Name'
    private static final String ELASTIC_BEANSTALK_ENVIRONMENT_ID_TAG_KEY = 'elasticbeanstalk:environment-id'

    @Delegate
    AmazonEC2 amazonEC2

    EnhancedAmazonEC2(AmazonEC2 amazonEC2) {
        this.amazonEC2 = amazonEC2
    }

    Instance findInstanceByPublicIpAddress(String ipAddress) {
        Instance instance = null

        List<Reservation> reservations = describeInstances().reservations
        Iterator<Reservation> iterator = reservations.iterator()

        while (!instance && iterator.hasNext()) {
            Reservation reservation = iterator.next()

            instance = reservation.instances.find {
                it.publicIpAddress == ipAddress
            }
        }

        return instance
    }

    List<SecurityGroup> findSecurityGroupsByEnvironmentId(String environmentId) {
        return describeSecurityGroups().securityGroups.findAll { securityGroup ->
            securityGroup.tags.find { tag -> tag.key == ELASTIC_BEANSTALK_ENVIRONMENT_ID_TAG_KEY }
        }
    }

    SecurityGroup findSecurityGroupByGroupName(String groupName) {
        return describeSecurityGroups().securityGroups.find {
            it.groupName == groupName
        }
    }

    List<Subnet> findSubnetsByVpcId(String vpcId) {
        return describeSubnets().subnets.findAll {
            it.vpcId == vpcId
        }
    }

    Subnet findSubnetByVpcIdAndSubnetName(String vpcId, String subnetName) {
        return findSubnetsByVpcId(vpcId).find { subnet ->
            subnet.tags.find { tag -> tag.key == NAME_TAG_KEY && tag.value == subnetName }
        }
    }
}
