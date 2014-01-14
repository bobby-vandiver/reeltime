package in.reeltime.storage

class InputStorageService {

    def storageService
    def grailsApplication

    def store(InputStream inputStream, String path) {
        def inputBase = grailsApplication.config.reeltime.storage.input
        storageService.store(inputStream, inputBase, path)
    }
}
