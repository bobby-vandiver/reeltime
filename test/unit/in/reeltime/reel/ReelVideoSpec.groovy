package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.video.Video

@TestFor(ReelVideo)
@Mock([Reel, Video])
class ReelVideoSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return ReelVideo
    }

    @Override
    Class getLeftPropertyClass() {
        return Reel
    }

    @Override
    Class getRightPropertyClass() {
        return Video
    }

    @Override
    String getLeftPropertyName() {
        return 'reel'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
