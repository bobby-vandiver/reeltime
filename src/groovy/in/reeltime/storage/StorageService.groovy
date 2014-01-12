package in.reeltime.storage

interface StorageService {

    boolean exists(String base, String relative)

    void store(InputStream inputStream, String base, String relative)
}
