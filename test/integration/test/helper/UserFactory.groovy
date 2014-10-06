package test.helper

import in.reeltime.oauth2.Client
import in.reeltime.reel.Audience
import in.reeltime.reel.Reel
import in.reeltime.user.User

class UserFactory {

    static User createTestUser() {
        createUser('someone')
    }

    static User createUser(String username, String displayName = null) {

        def client = new Client(
                clientName: username + '-client-name',
                clientId: username + 'test-client-id',
                clientSecret: username + 'test-client-secret'
        ).save()

        def reel = new Reel(
                name: Reel.UNCATEGORIZED_REEL_NAME,
                audience: new Audience(members: [])
        )

        if(!displayName) {
            displayName = "$username display"
        }

        new User(username: username, password: 'secret', displayName: displayName, email: "$username@test.com")
                .addToClients(client)
                .addToReels(reel)
                .save()
    }
}
