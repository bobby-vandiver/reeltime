package in.reeltime.video

import in.reeltime.user.User

class VideoCreationCommand {
    User creator
    String title
    String description
    InputStream videoStream
}
