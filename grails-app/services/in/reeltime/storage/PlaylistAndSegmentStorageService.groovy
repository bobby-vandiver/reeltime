package in.reeltime.storage

class PlaylistAndSegmentStorageService {

    def storageService
    def playlistBase

    InputStream load(String path) {
        storageService.load(playlistBase, path)
    }

    boolean exists(String path) {
        storageService.exists(playlistBase, path)
    }
}
