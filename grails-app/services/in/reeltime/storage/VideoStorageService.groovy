package in.reeltime.storage

class VideoStorageService {

    def storageService
    def grailsApplication

    def storeVideoStream(InputStream videoStream, String path) {
        def inputBase = grailsApplication.config.storage.input.masterVideos
        storageService.store(videoStream, inputBase, path)
    }
}
