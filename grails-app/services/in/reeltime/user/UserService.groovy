package in.reeltime.user

import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel
import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class UserService {

    def springSecurityService

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }

    User getCurrentUser() {
        springSecurityService.currentUser as User
    }

    User createAndSaveUser(String username, String password, String email, Client client, Reel reel) {
        new User(username: username, password: password, email: email)
                .addToClients(client)
                .addToReels(reel)
                .save()
    }

    User loadUser(String username) {
        def user = User.findByUsername(username)
        if(!user) {
            throw new UserNotFoundException("User [$username] not found")
        }
        return user
    }

    void storeUser(User user) {
        user.save()
    }
}
