package in.reeltime.storage

class OutputStorageService {

    def storageService
    def outputBase

    InputStream load(String path) {
        storageService.load(outputBase, path)
    }

    boolean exists(String path) {
        storageService.exists(outputBase, path)
    }
}
