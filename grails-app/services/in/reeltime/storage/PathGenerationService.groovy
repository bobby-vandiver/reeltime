package in.reeltime.storage

class PathGenerationService {

    def storageService

    def inputBase
    def outputBase

    String getUniqueInputPath() {
        generateRandomUniquePath(inputBase)
    }

    String getUniqueOutputPath() {
        generateRandomUniquePath(outputBase)
    }

    private String generateRandomUniquePath(base) {
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
