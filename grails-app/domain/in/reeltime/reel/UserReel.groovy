package in.reeltime.reel

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.user.User

@ToString(includeNames = true)
class UserReel extends AbstractJoinDomain implements Serializable {

    User owner
    Reel reel

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        owner nullable: false
        reel nullable: false
    }

    static mapping = {
        id composite: ['owner', 'reel']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'owner'
    }

    @Override
    String getRightPropertyName() {
        return 'reel'
    }
}
