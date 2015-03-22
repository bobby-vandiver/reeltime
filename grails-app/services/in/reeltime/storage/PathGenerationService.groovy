package in.reeltime.storage

class PathGenerationService {

    def storageService

    def videoBase
    def playlistBase

    String getUniqueVideoPath() {
        generateRandomUniquePath(videoBase)
    }

    String getUniquePlaylistPath() {
        generateRandomUniquePath(playlistBase)
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
