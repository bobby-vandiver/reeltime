package in.reeltime.user

import in.reeltime.oauth2.Client

class UserService {

    def reelService

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }

    User createAndSaveUser(String username, String password, String email, Client client) {
        def uncategorizedReel = reelService.createReel('Uncategorized')
        new User(username: username, password: password, email: email)
                .addToClients(client)
                .addToReels(uncategorizedReel)
                .save()
    }

    void updateUser(User user) {
        user.save()
    }
}
