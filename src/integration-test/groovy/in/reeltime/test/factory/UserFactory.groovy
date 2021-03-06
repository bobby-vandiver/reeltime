package in.reeltime.test.factory

import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel
import in.reeltime.reel.UserReel
import in.reeltime.user.User

class UserFactory {

    static User createTestUser() {
        createUser('someone')
    }

    static User createUser(String username, String displayName = null) {
        if(!displayName) {
            displayName = "$username display"
        }
        createUser(username, 'secret', displayName, "$username@test.com")
    }
    static User createUser(String username, String password, String displayName, String email) {
        def clientId = username + 'test-client-id'
        def clientSecret = username + 'test-client-secret'
        createUser(username, password, displayName, email, clientId, clientSecret)
    }

    static User createUser(String username, String password, String displayName, String email,
                           String clientId, String clientSecret) {
        def client = new Client(
                clientName: username + '-client-name',
                clientId: clientId,
                clientSecret: clientSecret
        ).save()

        assert client

        def reel = new Reel(name: Reel.UNCATEGORIZED_REEL_NAME).save()

        assert reel

        def user = new User(username: username, password: password, displayName: displayName, email: email)
                .addToClients(client)
                .save()

        new UserReel(owner: user, reel: reel).save()
        return user
    }
}
