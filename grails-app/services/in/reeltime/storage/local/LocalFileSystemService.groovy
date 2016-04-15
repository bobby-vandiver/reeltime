package in.reeltime.storage.local

import grails.transaction.Transactional

import static java.io.File.separator

@Transactional
class LocalFileSystemService {

    def videoBasePath
    def playlistBasePath

    String getDirectory(String parent, String child) {
        def path = parent + separator + child
        def lastSlash = path.lastIndexOf(separator)
        return path.substring(0, lastSlash)
    }

    String getFilename(String path) {
        def lastSlash = path.lastIndexOf(separator)
        def pathIsFilename = (lastSlash == -1)
        pathIsFilename ? path : path.substring(lastSlash)
    }

    File createDirectory(String path) {
        log.debug("Creating directory [$path]")
        def directory = new File(path)
        directory.deleteOnExit()
        boolean created = directory.mkdirs()
        log.debug("Created flag: $created")
        return directory
    }

    File createFile(String directory, String filename) {
        log.debug("Creating file [$filename]")
        def file = new File(directory, filename)
        file.deleteOnExit()
        return file
    }

    void deleteFile(String directory, String filename) {
        log.debug("Deleting file [$filename]")
        def file = new File(directory, filename)
        file.delete()
    }

    String getAbsolutePathToInputFile(String path) {
        "${videoBasePath}${separator}${path}"
    }

    String getAbsolutePathToOutputFile(String path) {
        "${playlistBasePath}${separator}${path}"
    }

}
