package in.reeltime.thumbnail

enum ThumbnailResolution {

    RESOLUTION_1X(75, 75),
    RESOLUTION_2X(150, 150),
    RESOLUTION_3X(225, 225)

    final int width
    final int height

    private ThumbnailResolution(int width, int height) {
        this.width = width
        this.height = height
    }
}
