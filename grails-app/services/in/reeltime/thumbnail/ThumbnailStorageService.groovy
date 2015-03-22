package in.reeltime.thumbnail

class ThumbnailStorageService {

    def pathGenerationService
    def storageService

    def thumbnailBase

    String getUniqueThumbnailPath() {
        pathGenerationService.generateRandomUniquePath(thumbnailBase)
    }

    InputStream load(String path) {
        storageService.load(thumbnailBase, path)
    }

    void store(InputStream inputStream, String path) {
        storageService.store(inputStream, thumbnailBase, path)
    }
}
