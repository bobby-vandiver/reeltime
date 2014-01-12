package in.reeltime.video

import in.reeltime.user.User

class VideoService {

    def storageNameService
    def videoStorageService
    def transcoderService

    def grailsApplication

    def createVideo(User creator, String title, InputStream videoStream) {

        def masterPath = storageNameService.uniqueInputPath
        videoStorageService.storeVideoStream(videoStream, masterPath)

        def video = new Video(creator: creator, title: title, masterPath: masterPath).save()

        def outputPath = storageNameService.uniqueOutputPath
        transcoderService.transcode(video, outputPath)

        return video
    }
}
