package in.reeltime.storage

class VideoStorageService {

    def storageService
    def videoBase

    def store(InputStream inputStream, String path) {
        storageService.store(inputStream, videoBase, path)
    }
}
