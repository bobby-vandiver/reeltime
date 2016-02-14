package in.reeltime.test.file

class FileLoader {

    static File imageFile(String filename) {
        getFile('images', filename)
    }

    static File videoFile(String filename) {
        getFile('videos', filename)
    }

    private static File getFile(String type, String filename) {
        new File("src/test/resources/files/$type/$filename")
    }
}
