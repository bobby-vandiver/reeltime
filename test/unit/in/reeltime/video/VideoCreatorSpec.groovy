package in.reeltime.video

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.user.User

@TestFor(VideoCreator)
@Mock([Video, User])
class VideoCreatorSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return VideoCreator
    }

    @Override
    Class getLeftPropertyClass() {
        return Video
    }

    @Override
    Class getRightPropertyClass() {
        return User
    }

    @Override
    String getLeftPropertyName() {
        return 'video'
    }

    @Override
    String getRightPropertyName() {
        return 'creator'
    }
}
