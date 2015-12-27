package in.reeltime.thumbnail

import in.reeltime.video.Video

class ThumbnailRemovalService {

    def resourceRemovalService
    def thumbnailStorageService

    void removeThumbnailsForVideo(Video video) {
        def thumbnailBase = thumbnailStorageService.thumbnailBase

        log.info "Scheduling removal of master thumbnail for video [$video.id]"
        resourceRemovalService.scheduleForRemoval(thumbnailBase, video.masterThumbnailPath)

        log.info "Scheduling removal of thumbnails for video [$video]"
        video.thumbnails.each { thumbnail ->
            resourceRemovalService.scheduleForRemoval(thumbnailBase, thumbnail.uri)

            ThumbnailVideo.findByVideoAndThumbnail(video, thumbnail).delete()
            thumbnail.delete()
        }
    }
}
