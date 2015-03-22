package in.reeltime.storage

class PathGenerationService {

    def storageService

    // FIXME: Limit number of retries to avoid infinite loop!
    String generateRandomUniquePath(base) {
        def path = randomUUIDString()
        while (storageService.exists(base, path)) {
            path = randomUUIDString()
        }
        return path
    }

    private static String randomUUIDString() {
        UUID.randomUUID() as String
    }
}
