package in.reeltime.user

import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel

class UserService {

    def reelService

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }

    User createAndSaveUser(String username, String password, String email, Client client) {
        def user = new User(username: username, password: password, email: email, clients: [client])
        def uncategorizedReel = reelService.createReel(user, 'Uncategorized')

        user.addToReels(uncategorizedReel)
        user.save()
    }

    void updateUser(User user) {
        user.save()
    }

    Collection<Reel> listReels(String username) {
        User.findByUsername(username).reels
    }
}
