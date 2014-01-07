package in.reeltime.storage

interface StorageService {

    boolean available(String basePath, String resourcePath)

    void store(InputStream inputStream, String basePath, String resourcePath)
}
