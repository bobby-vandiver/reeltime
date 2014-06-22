package in.reeltime.user

import in.reeltime.oauth2.Client

class UserRegistrationService {

    User register(String username, String password, Client client) {
        new User(username: username, password: password, clients: [client]).save()
    }
}
