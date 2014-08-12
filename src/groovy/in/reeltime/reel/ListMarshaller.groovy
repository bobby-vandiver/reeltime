package in.reeltime.reel

import in.reeltime.video.Video

// TODO: Use marshallers plugin if this becomes more involved
class ListMarshaller {

    static List marshallReelList(Collection<Reel> reels) {
        reels.collect([]) { reel ->
            [reelId: reel.id, name: reel.name]
        }
    }

    static List marshallVideoList(Collection<Video> videos) {
        videos.collect([]) { video ->
            [videoId: video.id]
        }
    }
}
