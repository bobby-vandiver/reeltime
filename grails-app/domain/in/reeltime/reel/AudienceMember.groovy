package in.reeltime.reel

import groovy.transform.ToString
import in.reeltime.common.AbstractJoinDomain
import in.reeltime.user.User

@ToString(includeNames = true)
class AudienceMember extends AbstractJoinDomain implements Serializable {

    private static final long serialVersionUID = 1

    Reel reel
    User member

    static transients = ['leftPropertyName', 'rightPropertyName']

    static constraints = {
        reel nullable: false
        member nullable: false
    }

    static mapping = {
        id composite: ['reel', 'member']
        version false
    }

    @Override
    String getLeftPropertyName() {
        return 'reel'
    }

    @Override
    String getRightPropertyName() {
        return 'member'
    }
}
