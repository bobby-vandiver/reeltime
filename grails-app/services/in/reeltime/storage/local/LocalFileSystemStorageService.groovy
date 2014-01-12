package in.reeltime.storage.local

import in.reeltime.storage.StorageService

import static java.io.File.separator

class LocalFileSystemStorageService implements StorageService {

    @Override
    boolean exists(String parent, String child) {
        new File(parent, child).exists()
    }

    @Override
    void store(InputStream inputStream, String parent, String child) {

        def directory = getDirectory(parent, child)
        def filename = getFilename(child)

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
