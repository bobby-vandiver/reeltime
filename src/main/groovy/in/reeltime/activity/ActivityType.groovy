package in.reeltime.activity

enum ActivityType {

    // The value represents the chronological ordering used for resolving activities
    // that occur at precisely the same moment in time.
    //
    // A lower value means that the associated activity occurs earlier in chronological
    // order than the other activity types.
    //
    // The large gaps between each activity's value is to facilitate the addition of new
    // activity types in the future

    CreateReel(0),
    JoinReelAudience(50),
    AddVideoToReel(100)

    final int value

    private ActivityType(int value) {
        this.value = value
    }

    static ActivityType byValue(int value) {
        def type = values().find { it.value == value }
        if(!type) {
            throw new IllegalArgumentException("Value [$value] does not map to an ActivityType")
        }
        return type
    }
}
