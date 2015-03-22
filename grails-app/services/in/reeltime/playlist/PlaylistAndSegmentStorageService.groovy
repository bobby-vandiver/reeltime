package in.reeltime.playlist

class PlaylistAndSegmentStorageService {

    def pathGenerationService
    def storageService

    def playlistBase

    String getUniquePlaylistPath() {
        pathGenerationService.generateRandomUniquePath(playlistBase)
    }

    InputStream load(String path) {
        storageService.load(playlistBase, path)
    }

    boolean exists(String path) {
        storageService.exists(playlistBase, path)
    }
}
