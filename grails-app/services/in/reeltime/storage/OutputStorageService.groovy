package in.reeltime.storage

class OutputStorageService {

    def storageService
    def outputBase

    def load(String path) {
        storageService.load(outputBase, path)
    }
}
