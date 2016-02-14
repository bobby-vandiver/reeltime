package in.reeltime.test.factory

import in.reeltime.user.User
import in.reeltime.video.Video
import in.reeltime.video.VideoCreator

class VideoFactory {

    static Video createVideo(User creator, String title, boolean available = true) {
        def video = new Video(
                title: title,
                masterPath: title + '-' + System.currentTimeMillis(),
                masterThumbnailPath: title + '-thumbnail-' + System.currentTimeMillis(),
                available: available
        ).save()

        new VideoCreator(video: video, creator: creator).save()
        return video
    }
}
