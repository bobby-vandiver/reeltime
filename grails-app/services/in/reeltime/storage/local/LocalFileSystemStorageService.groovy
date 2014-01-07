package in.reeltime.storage.local

import in.reeltime.storage.StorageService

import static java.io.File.separator

class LocalFileSystemStorageService implements StorageService {

    @Override
    boolean available(String basePath, String resourcePath) {
        return false
    }

    @Override
    void store(InputStream inputStream, String basePath, String resourcePath) {

        def directory = getDirectory(basePath, resourcePath)
        def filename = getFilename(resourcePath)

        createDirectory(directory)
        createFile(directory, filename).withOutputStream { outputStream -> outputStream << inputStream }
    }

    private String getDirectory(String parent, String child) {
        def path = parent + separator + child
        def lastSlash = path.lastIndexOf(separator)
        return path.substring(0, lastSlash)
    }

    private String getFilename(String path) {
        def lastSlash = path.lastIndexOf(separator)
        def pathIsFilename = (lastSlash == -1)
        pathIsFilename ? path : path.substring(lastSlash)
    }

    private void createDirectory(String path) {
        log.debug("Creating directory [$path]")
        def directory = new File(path)
        directory.deleteOnExit()
        directory.mkdirs()
    }

    private File createFile(String directory, String filename) {
        log.debug("Creating file [$filename]")
        def file = new File(directory, filename)
        file.deleteOnExit()
        return file
    }
}
