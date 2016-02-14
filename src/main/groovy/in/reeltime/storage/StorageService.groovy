package in.reeltime.storage

interface StorageService {

    InputStream load(String base, String relative)

    boolean exists(String base, String relative)

    void store(InputStream inputStream, String base, String relative)

    void delete(String base, String relative)
}
