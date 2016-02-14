package in.reeltime.thumbnail

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.video.Video

@TestFor(ThumbnailVideo)
@Mock([Thumbnail, Video])
class ThumbnailVideoSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return ThumbnailVideo
    }

    @Override
    Class getLeftPropertyClass() {
        return Thumbnail
    }

    @Override
    Class getRightPropertyClass() {
        return Video
    }

    @Override
    String getLeftPropertyName() {
        return 'thumbnail'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
