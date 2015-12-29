package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.user.User

@TestFor(UserReel)
@Mock([User, Reel])
class UserReelSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return UserReel
    }

    @Override
    Class getLeftPropertyClass() {
        return User
    }

    @Override
    Class getRightPropertyClass() {
        return Reel
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
