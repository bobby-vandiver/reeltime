package in.reeltime.deployment.aws.client

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.model.Role
import com.amazonaws.services.identitymanagement.model.ServerCertificateMetadata

class EnhancedAmazonIdentityManagement implements AmazonIdentityManagement {

    @Delegate
    AmazonIdentityManagement amazonIdentityManagement

    EnhancedAmazonIdentityManagement(AmazonIdentityManagement amazonIdentityManagement) {
        this.amazonIdentityManagement = amazonIdentityManagement
    }

    boolean roleExists(String roleName) {
        return findRoleByName(roleName) != null
    }

    String findRoleArnByName(String roleName) {
        return findRoleByName(roleName)?.arn
    }

    String findServerCertificateArnByName(String certificateName) {
        return findServerCertificateMetadataByName(certificateName)?.arn
    }

    private Role findRoleByName(String roleName) {
        return listRoles().roles.find { it.roleName == roleName }
    }

    private ServerCertificateMetadata findServerCertificateMetadataByName(String certificateName) {
        return listServerCertificates().serverCertificateMetadataList.find {
            it.serverCertificateName == certificateName
        }
    }
}
