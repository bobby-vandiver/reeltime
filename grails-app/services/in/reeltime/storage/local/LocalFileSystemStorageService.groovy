package in.reeltime.storage.local

import in.reeltime.storage.StorageService

import static java.io.File.separator

class LocalFileSystemStorageService implements StorageService {

    def localFileSystemService

    @Override
    InputStream load(String parent, String child) {
        log.debug("Loading input stream from parent [$parent] with child [$child]")
        new File(parent, child).newInputStream()
    }

    @Override
    boolean exists(String parent, String child) {
        log.debug("Checking existence of file with parent [$parent] and child [$child]")
        new File(parent, child).exists()
    }

    @Override
    void store(InputStream inputStream, String parent, String child) {

        log.debug("Storing input stream to parent [$parent] with child [$child]")

        def directory = localFileSystemService.getDirectory(parent, child)
        def filename = localFileSystemService.getFilename(child)

        localFileSystemService.createDirectory(directory)
        localFileSystemService.createFile(directory, filename).withOutputStream { outputStream -> outputStream << inputStream }
    }
}
