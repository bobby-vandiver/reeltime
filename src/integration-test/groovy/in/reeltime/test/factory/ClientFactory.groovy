package in.reeltime.test.factory

import in.reeltime.oauth2.Client

class ClientFactory {

    static Client createClient(String clientId, String clientSecret) {
        new Client(
                clientName: clientId + '-name',
                clientId: clientId,
                clientSecret: clientSecret
        ).save()
    }
}
