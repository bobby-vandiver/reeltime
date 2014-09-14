package test.helper

import in.reeltime.oauth2.Client
import in.reeltime.reel.Audience
import in.reeltime.reel.Reel
import in.reeltime.user.User

class UserFactory {

    static User createTestUser() {

        def client = new Client(
                clientName: 'test-client-name',
                clientId: 'test-client-id',
                clientSecret: 'test-client-secret'
        ).save()

        def reel = new Reel(
                name: Reel.UNCATEGORIZED_REEL_NAME,
                audience: new Audience(members: [])
        )

        new User(username: 'someone', password: 'secret', email: 'someone@test.com')
                .addToClients(client)
                .addToReels(reel)
                .save()
    }
}
