package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec
import in.reeltime.video.Video

@TestFor(PlaylistVideo)
@Mock([Playlist, Video])
class PlaylistVideoSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return PlaylistVideo
    }

    @Override
    Class getLeftPropertyClass() {
        return Playlist
    }

    @Override
    Class getRightPropertyClass() {
        return Video
    }

    @Override
    String getLeftPropertyName() {
        return 'playlist'
    }

    @Override
    String getRightPropertyName() {
        return 'video'
    }
}
