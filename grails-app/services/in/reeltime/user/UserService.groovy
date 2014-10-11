package in.reeltime.user

import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel
import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class UserService {

    def springSecurityService
    def maxUsersPerPage

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }

    List<User> listUsers(int page) {
        int offset = (page - 1) * maxUsersPerPage
        User.list(max: maxUsersPerPage, offset: offset, sort: 'username')
    }

    User getCurrentUser() {
        springSecurityService.currentUser as User
    }

    User createAndSaveUser(String username, String password, String displayName,
                           String email, Client client, Reel reel) {
        new User(username: username, password: password, displayName: displayName, email: email)
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
