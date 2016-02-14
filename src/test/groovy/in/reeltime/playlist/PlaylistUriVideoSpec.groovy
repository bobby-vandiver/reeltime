package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.video.Video

@TestFor(PlaylistUriVideo)
@Mock([PlaylistUri, Video])
class PlaylistUriVideoSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return PlaylistUriVideo
    }

    @Override
    Class getLeftPropertyClass() {
        return PlaylistUri
    }

    @Override
    Class getRightPropertyClass() {
        return Video
    }

    @Override
    String getLeftPropertyName() {
        return 'playlistUri'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
