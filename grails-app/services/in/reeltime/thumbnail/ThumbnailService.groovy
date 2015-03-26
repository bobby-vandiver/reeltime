package in.reeltime.thumbnail

import in.reeltime.video.Video
import in.reeltime.exceptions.ThumbnailNotFoundException
import static in.reeltime.thumbnail.ThumbnailResolution.*

class ThumbnailService {

    def thumbnailGenerationService
    def videoService

    private static List<ThumbnailResolution> resolutions = [RESOLUTION_1X, RESOLUTION_2X, RESOLUTION_3X]

    void addThumbnails(Video video) {
        resolutions.each { resolution ->
            def uri = thumbnailGenerationService.generateThumbnail(video.masterThumbnailPath, resolution)

            def thumbnail = new Thumbnail(uri: uri, resolution: resolution)
            video.addToThumbnails(thumbnail)

            storeThumbnail(thumbnail)
        }
        videoService.storeVideo(video)
    }

    Thumbnail loadThumbnail(Long videoId, ThumbnailResolution resolution) {
        def video = videoService.loadVideo(videoId)
        def thumbnail = Thumbnail.findByVideoAndResolution(video, resolution)

        if(!thumbnail) {
            def message = "Thumbnail for video [${videoId}] and resolution [${resolution.name()}] not found"
            throw new ThumbnailNotFoundException(message)
        }

        return thumbnail
    }

    void storeThumbnail(Thumbnail thumbnail) {
        thumbnail.save()
    }
}
