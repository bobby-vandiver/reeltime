package in.reeltime.account

import in.reeltime.oauth2.Client
import in.reeltime.user.User

class AccountRemovalService {

    def authenticationService

    def activityService
    def audienceService
    def userFollowingService

    def tokenRemovalService
    def videoRemovalService

    void removeAccountForCurrentUser() {
        def currentUser = authenticationService.currentUser
        def username = currentUser.username

        log.info "Removing confirmation codes for user [${username}]"
        deleteConfirmationCodesForUser(currentUser)

        log.info "Removing tokens associated with user [${username}]"
        tokenRemovalService.removeAllTokensForUser(currentUser)

        log.info "Deleting activity for user [${username}]"
        activityService.deleteAllUserActivity(currentUser)

        log.info "Removing follower/followee relationships for [${username}]"
        userFollowingService.removeFollowerFromAllFollowings(currentUser)
        userFollowingService.removeFolloweeFromAllFollowings(currentUser)

        log.info "Remove user [${username}] as an audience member from all reels"
        audienceService.removeMemberFromAllAudiences(currentUser)

        log.info "Removing videos for user [${username}]"
        deleteVideosForUser(currentUser)

        log.info "Removing clients for user [${username}]"
        deleteClientsForUser(currentUser)

        log.info "Deleting user [${username}]"
        currentUser.delete()

        log.info "Finished removing account for user [${username}]"
    }

    private static void deleteConfirmationCodesForUser(User user) {
        def confirmationCodes = AccountCode.findAllByUser(user)
        confirmationCodes.each { code ->
            log.debug "Deleting account confirmation code [${code.id}]"
            code.delete()
        }
    }

    private static void deleteClientsForUser(User user) {
        def clientsToRemove = []
        clientsToRemove.addAll(user.clients)

        clientsToRemove.each { Client client ->
            log.debug "Removing client [${client.id}] from user [${user.username}]"
            user.removeFromClients(client)

            log.debug "Deleting client [${client.id}]"
            client.delete()
        }
    }

    private void deleteVideosForUser(User user) {
        def videosToRemove = []
        if(user?.videos) {
            videosToRemove.addAll(user.videos)
        }

        videosToRemove.each { video ->
            log.debug "Removing video [${video.id}]"
            videoRemovalService.removeVideo(video)
        }
    }
}
