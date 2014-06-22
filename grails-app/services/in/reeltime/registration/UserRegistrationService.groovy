package in.reeltime.registration

import in.reeltime.oauth2.Client
import in.reeltime.user.User

class UserRegistrationService {

    User register(String username, String password, Client client) {
        new User(username: username, password: password, clients: [client]).save()
    }

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }
}
