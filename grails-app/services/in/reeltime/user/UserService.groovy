package in.reeltime.user

import in.reeltime.oauth2.Client

class UserService {

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }

    User createUser(String username, String password, Client client) {
        new User(username: username, password: password, clients: [client]).save()
    }
}
