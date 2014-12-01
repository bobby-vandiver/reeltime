package in.reeltime.video

import in.reeltime.exceptions.VideoNotFoundException

class VideoService {

    def maxVideosPerPage

    List<Video> listVideos(int page) {
        int offset = (page - 1) * maxVideosPerPage
        Video.findAllByAvailable(true, [max: maxVideosPerPage, offset: offset, sort: 'dateCreated', order: 'desc'])
    }

    Video loadVideo(Long videoId) {
        def video = Video.findById(videoId)
        if(!video) {
            throw new VideoNotFoundException("Video [$videoId] not found")
        }
        return video
    }

    void storeVideo(Video video) {
        video.save()
    }
}
