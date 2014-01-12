package in.reeltime.storage

class PathGenerationService {

    def storageService
    def grailsApplication

    String getUniqueInputPath() {
        def inputBase = grailsApplication.config.storage.input
        generateRandomUniquePath(inputBase)
    }

    String getUniqueOutputPath() {
        def outputBase = grailsApplication.config.storage.output
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
