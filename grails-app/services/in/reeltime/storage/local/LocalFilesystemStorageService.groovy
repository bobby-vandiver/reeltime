package in.reeltime.storage.local

import in.reeltime.storage.StorageService

class LocalFilesystemStorageService implements StorageService {

    @Override
    void store(InputStream inputStream, String basePath, String resourcePath) {
        createDirectory(basePath)
        createFile(basePath, resourcePath).withOutputStream { outputStream ->
            outputStream << inputStream
        }
    }

    private void createDirectory(String path) {
        log.debug("Creating directory [$path]")
        def directory = new File(path)
        directory.deleteOnExit()
        directory.mkdir()
    }

    private File createFile(String directory, String filename) {
        log.debug("Creating file [$filename]")
        def file = new File(directory, filename)
        file.deleteOnExit()
        return file
    }
}
