Production Environment Set Up
=============================

The following steps must be performed in the AWS Console.

Additional pre-deployment configuration is handled by the script(s) located in the 
`reeltime-config` repository.

The final configuration necessary for deploying to the production environment is handled by as
part of the `deploy` Grails script. 

*Note*: The AWS CLI is used throughout to demonstrate the configuration of the relevant
components. These are taken from a working load-balanced, production environment.

Create the VPC
--------------

1. From the VPC Dashboard, select "Start Up VPC Wizard".
 
2. Select the "VPC with Public and Private Subnets" configuration.

3. Press the "Select" button.

4. Assign the VPC, public subnet and private subnets names.

5. Make sure the public and private subnets are in the *same* Availability Zone.
   This is necessary to ensure the load balancer launched in the public subnet
   and the EC2 instances launched in the private subnet are able to communicate.
   
6. Press the "Create VPC" button.

Executing `aws ec2 describe-vpcs` will look like this:

```
{
    "Vpcs": [
        {
            "VpcId": "vpc-20f35345", 
            "InstanceTenancy": "default", 
            "Tags": [
                {
                    "Value": "VPC Test 2", 
                    "Key": "Name"
                }
            ], 
            "State": "available", 
            "DhcpOptionsId": "dopt-2f646c4d", 
            "CidrBlock": "10.0.0.0/16", 
            "IsDefault": false
        } 
    ]
}
```

Executing `aws ec2 describe-subnets` will look like this: 

```
{
    "Subnets": [
        {
            "VpcId": "vpc-20f35345", 
            "Tags": [
                {
                    "Value": "Private subnet", 
                    "Key": "Name"
                }
            ], 
            "CidrBlock": "10.0.1.0/24", 
            "MapPublicIpOnLaunch": false, 
            "DefaultForAz": false, 
            "State": "available", 
            "AvailabilityZone": "us-east-1d", 
            "SubnetId": "subnet-5531327d", 
            "AvailableIpAddressCount": 250
        }, 
        {
            "VpcId": "vpc-20f35345", 
            "Tags": [
                {
                    "Value": "Public subnet", 
                    "Key": "Name"
                }
            ], 
            "CidrBlock": "10.0.0.0/24", 
            "MapPublicIpOnLaunch": false, 
            "DefaultForAz": false, 
            "State": "available", 
            "AvailabilityZone": "us-east-1d", 
            "SubnetId": "subnet-5631327e", 
            "AvailableIpAddressCount": 248
        } 
    ]
}
```

Name the NAT
------------

When the wizard is finished creating the VPC, there will be a NAT instance running in the public subnet.

1. Go to the "Your VPCs" menu and write down the VPC ID for the newly created VPC.
   
2. Go to the "Instances" menu from the EC2 Dashboard.
   
3. Locate the NAT instance created in the new VPC.

4. Give the NAT instance a name.

Create Security Groups
----------------------

All security groups created below should be assigned the newly created VPC.

The JSON output is obtained by using the AWS CLI and executing: `aws ec2 describe-security-groups`

### Create NAT Security Group

1. Go to "Security Groups" from either the VPC Dashboard or the EC2 Dashboard.
 
2. Create a security group for the NAT instance.
 
3. Remove all inbound rules.
 
4. Ensure an outbound rule exists allowing the EC2 instances in the private
   subnet to communicate with the internet, i.e. the other AWS resources used.
   
The security group named "NAT-SG" will look like this:
 
```
        {
            "IpPermissionsEgress": [
                {
                    "IpProtocol": "-1", 
                    "IpRanges": [
                        {
                            "CidrIp": "0.0.0.0/0"
                        }
                    ], 
                    "UserIdGroupPairs": []
                }
            ], 
            "Description": "NAT security group for VPC Test 2", 
            "Tags": [
                {
                    "Value": "NAT-SG", 
                    "Key": "Name"
                }
            ], 
            "IpPermissions": [], 
            "GroupName": "NAT-SG", 
            "VpcId": "vpc-20f35345", 
            "OwnerId": "166209233708", 
            "GroupId": "sg-83dc98e6"
        }
```        

### Update Default VPC Security Group
   
1. Locate the default security group for the newly created VPC.
 
2. Remove all inbound rules.

3. Add one inbound rule allowing all traffic with the NAT security group as the source.

4. Ensure an outbound rule exists allow all outbound traffic.

The default security group for the VPC will look like this:

```
        {
            "IpPermissionsEgress": [
                {
                    "IpProtocol": "-1", 
                    "IpRanges": [
                        {
                            "CidrIp": "0.0.0.0/0"
                        }
                    ], 
                    "UserIdGroupPairs": []
                }
            ], 
            "Description": "default VPC security group", 
            "IpPermissions": [
                {
                    "IpProtocol": "-1", 
                    "IpRanges": [], 
                    "UserIdGroupPairs": [
                        {
                            "UserId": "166209233708", 
                            "GroupId": "sg-83dc98e6"
                        }
                    ]
                }
            ], 
            "GroupName": "default", 
            "VpcId": "vpc-20f35345", 
            "OwnerId": "166209233708", 
            "GroupId": "sg-30dc9855"
        }
```

Update Deployment Configuration
-------------------------------

Add the name of the NAT security group as the only launch security group name.
If the security group name is "NAT-SG", the configuration would look like this:

```
              launch: [
                      instanceProfileName: 'EC2-Instance-Test-Role',
                      securityGroupNames: ['NAT-SG']
              ],
```

Add the VPC ID and subnet names to the vpc configuration. If the VPC ID is "vpc-20f35345",
the public subnet name is "Public subnet" and the private subnet name is "Private subnet",
then the configuration would look like this:
   
```
            vpc: [
                    vpcId: 'vpc-20f35345',
                    loadBalancerSubnetName: 'Public subnet',
                    autoScalingSubnetName: 'Private subnet'
            ],
```