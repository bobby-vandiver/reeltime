package in.reeltime.reel


class AudienceService {

    def userService

    Audience createAudience() {
        new Audience(members: [])
    }
}
