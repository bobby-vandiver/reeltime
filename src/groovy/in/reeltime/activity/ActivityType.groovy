package in.reeltime.activity

enum ActivityType {

    CreateReel('create-reel'),
    AddVideoToReel('add-video-to-reel')

    private String toStringValue

    private ActivityType(String toStringValue) {
        this.toStringValue = toStringValue
    }

    @Override
    String toString() {
        return toStringValue
    }
}
