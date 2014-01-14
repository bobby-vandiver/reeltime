package in.reeltime.storage

class OutputStorageService {

    def storageService
    def grailsApplication

    def load(String path) {
        def outputBase = grailsApplication.config.reeltime.storage.output
        storageService.load(outputBase, path)
    }
}
