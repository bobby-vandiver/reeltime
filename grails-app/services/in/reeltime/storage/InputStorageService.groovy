package in.reeltime.storage

class InputStorageService {

    def storageService
    def inputBase

    def store(InputStream inputStream, String path) {
        storageService.store(inputStream, inputBase, path)
    }
}
