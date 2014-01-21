package in.reeltime.storage.local

import in.reeltime.video.Video

import static java.io.File.separator

class LocalFileSystemService {

    def grailsApplication

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
        directory.mkdirs()
        return directory
    }

    File createFile(String directory, String filename) {
        log.debug("Creating file [$filename]")
        def file = new File(directory, filename)
        file.deleteOnExit()
        return file
    }

    String getAbsolutePathToInputFile(String path) {
        def input = grailsApplication.config.reeltime.storage.input
        "${input}${separator}${path}"
    }

    String getAbsolutePathToOutputFile(String path) {
        def output = grailsApplication.config.reeltime.storage.output
        "${output}${separator}${path}"
    }

}
