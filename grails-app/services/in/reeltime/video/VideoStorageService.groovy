package in.reeltime.video

class VideoStorageService {

    def pathGenerationService
    def storageService

    def videoBase

    String getUniqueVideoPath() {
        pathGenerationService.generateRandomUniquePath(videoBase)
    }

    def store(InputStream inputStream, String path) {
        storageService.store(inputStream, videoBase, path)
    }
}
