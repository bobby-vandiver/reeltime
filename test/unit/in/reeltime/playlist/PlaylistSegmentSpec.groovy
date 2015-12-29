package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.common.AbstractJoinDomainSpec

@TestFor(PlaylistSegment)
@Mock([Playlist, Segment])
class PlaylistSegmentSpec extends AbstractJoinDomainSpec {

    @Override
    Class getJoinClass() {
        return PlaylistSegment
    }

    @Override
    Class getLeftPropertyClass() {
        return Playlist
    }

    @Override
    Class getRightPropertyClass() {
        return Segment
    }

    @Override
    String getLeftPropertyName() {
        return 'playlist'
    }

    @Override
    String getRightPropertyName() {
        return 'segment'
    }
}
