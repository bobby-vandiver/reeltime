package in.reeltime.user

import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel

class UserService {

    def reelService
    def springSecurityService

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }

    User createAndSaveUser(String username, String password, String email, Client client) {
        def user = new User(username: username, password: password, email: email, clients: [client])
        addReelToUserAndSave(user, 'Uncategorized')
        return user
    }

    User loadUser(String username) {
        def user = User.findByUsername(username)
        if(!user) {
            throw new UserNotFoundException("User [$username] not found")
        }
        return user
    }

    void updateUser(User user) {
        user.save()
    }

    Collection<Reel> listReels(String username) {
        loadUser(username).reels
    }

    void addReel(String reelName) {
        def currentUser = springSecurityService.currentUser as User
        addReelToUserAndSave(currentUser, reelName)
    }

    private void addReelToUserAndSave(User user, String reelName) {
        def reel = reelService.createReel(user, reelName)
        user.addToReels(reel)
        user.save()
    }
}
