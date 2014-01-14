package in.reeltime.video

import in.reeltime.user.User

class VideoService {

    def pathGenerationService
    def inputStorageService
    def transcoderService

    def grailsApplication

    def createVideo(User creator, String title, InputStream videoStream) {

        def masterPath = pathGenerationService.uniqueInputPath
        inputStorageService.store(videoStream, masterPath)

        def video = new Video(creator: creator, title: title, masterPath: masterPath).save()
        log.info("Created video with id [${video.id}] for user [${creator.username}]")

        def outputPath = pathGenerationService.uniqueOutputPath
        transcoderService.transcode(video, outputPath)

        return video
    }
}
