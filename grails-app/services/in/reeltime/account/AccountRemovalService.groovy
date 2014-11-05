package in.reeltime.account

class AccountRemovalService {

    def authenticationService

    def activityService
    def audienceService

    def clientService
    def userFollowingService

    def accountCodeRemovalService
    def reelRemovalService
    def tokenRemovalService
    def videoRemovalService

    void removeAccountForCurrentUser() {
        def currentUser = authenticationService.currentUser
        def username = currentUser.username

        log.info "Removing confirmation codes for user [${username}]"
        accountCodeRemovalService.removeConfirmationCodesForUser(currentUser)

        log.info "Removing tokens associated with user [${username}]"
        tokenRemovalService.removeAllTokensForUser(currentUser)

        log.info "Deleting activity for user [${username}]"
        activityService.deleteAllUserActivity(currentUser)

        log.info "Removing follower/followee relationships for [${username}]"
        userFollowingService.removeFollowerFromAllFollowings(currentUser)
        userFollowingService.removeFolloweeFromAllFollowings(currentUser)

        log.info "Remove user [${username}] as an audience member from all reels"
        audienceService.removeMemberFromAllAudiences(currentUser)

        log.info "Remove reels for user [${username}]"
        reelRemovalService.removeReelsForUser(currentUser)

        log.info "Removing videos for user [${username}]"
        videoRemovalService.removeVideosForUser(currentUser)

        log.info "Removing clients for user [${username}]"
        clientService.removeClientsForUser(currentUser)

        log.info "Deleting user [${username}]"
        currentUser.delete()

        log.info "Finished removing account for user [${username}]"
    }
}
