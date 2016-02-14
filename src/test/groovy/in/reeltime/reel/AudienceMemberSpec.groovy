package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.user.User

@TestFor(AudienceMember)
@Mock([Reel, User])
class AudienceMemberSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return AudienceMember
    }

    @Override
    Class getLeftPropertyClass() {
        return Reel
    }

    @Override
    Class getRightPropertyClass() {
        return User
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
