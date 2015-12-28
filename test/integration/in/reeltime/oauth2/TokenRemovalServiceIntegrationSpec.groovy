package in.reeltime.oauth2

import grails.test.spock.IntegrationSpec
import in.reeltime.reel.Reel
import in.reeltime.user.User
import spock.lang.Unroll

class TokenRemovalServiceIntegrationSpec extends IntegrationSpec {

    def tokenRemovalService

    Random randomNumberGenerator

    void setup() {
        randomNumberGenerator = new Random(0)
    }

    void "attempt to remove unknown access token"() {
        when:
        tokenRemovalService.removeAccessToken('unknown')

        then:
        notThrown(Exception)
    }

    void "remove access token with no refresh token"() {
        given:
        def username = 'user'
        def clientId = 'client'

        and:
        def accessToken = createAccessToken(username, clientId)

        and:
        def tokenValue = accessToken.value
        assert AccessToken.findByValue(tokenValue) != null

        when:
        tokenRemovalService.removeAccessToken(tokenValue)

        then:
        AccessToken.findByValue(tokenValue) == null
    }

    void "remove access token and associated refresh token"() {
        given:
        def username = 'user'
        def clientId = 'client'

        and:
        def refreshToken = createRefreshToken()

        and:
        def refreshTokenValue = refreshToken.value
        assert RefreshToken.findByValue(refreshTokenValue) != null

        and:
        def accessToken = createAccessToken(username, clientId, refreshTokenValue)

        and:
        def accessTokenValue = accessToken.value
        assert AccessToken.findByValue(accessTokenValue) != null

        when:
        tokenRemovalService.removeAccessToken(accessTokenValue)

        then:
        AccessToken.findByValue(accessTokenValue) == null
        RefreshToken.findByValue(refreshTokenValue) == null
    }

    void "user has multiple clients -- only revoke tokens for the specified client"() {
        given:
        def client1 = createClient('client1')
        def client2 = createClient('client2')

        and:
        def user = createUser('someone', client1.clientId)
        user.addToClients(client2)
        user.save()

        and:
        def refreshToken1 = createRefreshToken()
        def refreshToken2 = createRefreshToken()

        and:
        def accessToken1 = createAccessToken(user.username, client1.clientId, refreshToken1.value)
        def accessToken2 = createAccessToken(user.username, client2.clientId, refreshToken2.value)

        when:
        tokenRemovalService.removeAllTokensForClient(client2)

        then:
        AccessToken.findByValue(accessToken1.value) != null
        RefreshToken.findByValue(refreshToken1.value) != null

        and:
        AccessToken.findByValue(accessToken2.value) == null
        RefreshToken.findByValue(refreshToken2.value) == null
    }

    @Unroll
    void "remove all tokens associated with the specified user - [#clientCount] clients, [#accessTokenCount] access tokens and [#refreshTokenCount] refresh tokens"() {
        given:
        def username = 'someone'
        def baseClientId = 'someClientId'

        and:
        def clientIds = createClients(baseClientId, clientCount)
        def user = createUser(username, clientIds[0])

        and:
        def tokenIds = createAccessTokensAndRefreshTokens(username, clientIds, accessTokenCount, refreshTokenCount)

        when:
        tokenRemovalService.removeAllTokensForUser(user)

        then:
        assertAccessTokensAndRefreshTokensAreDeleted(tokenIds)

        where:
        clientCount |   accessTokenCount    |   refreshTokenCount
        1           |   1                   |   0
        1           |   1                   |   1
        1           |   2                   |   0
        1           |   2                   |   1
        1           |   2                   |   2

        2           |   1                   |   0
        2           |   1                   |   1
        2           |   2                   |   0
        2           |   2                   |   1
        2           |   2                   |   2

        5           |   1                   |   0
        5           |   1                   |   1
        5           |   10                  |   0
        5           |   10                  |   5
        5           |   10                  |   8
    }

    @Unroll
    void "remove all tokens associated with the specified client - [#accessTokenCount] access tokens and [#refreshTokenCount] refresh tokens"() {
        given:
        def username = 'someone'
        def clientId = 'someClientId'

        and:
        def client = createClient(clientId)
        createUser(username, clientId)

        and:
        def tokenIds = createAccessTokensAndRefreshTokens(username, [clientId], accessTokenCount, refreshTokenCount)

        when:
        tokenRemovalService.removeAllTokensForClient(client)

        then:
        assertAccessTokensAndRefreshTokensAreDeleted(tokenIds)

        where:
        accessTokenCount    |   refreshTokenCount
        1                   |   0
        1                   |   1
        2                   |   0
        2                   |   1
        2                   |   2
        1                   |   0
        1                   |   1
        2                   |   0
        2                   |   1
        2                   |   2
        1                   |   0
        1                   |   1
        10                  |   0
        10                  |   5
        10                  |   8
    }

    private Map createAccessTokensAndRefreshTokens(String username, Collection<String> clientIds,
                                                          int accessTokenCount, int refreshTokenCount) {
        if(refreshTokenCount > accessTokenCount) {
            throw new IllegalArgumentException("Refresh token count cannot exceed access token count")
        }

        Map tokenIds = [accessTokenIds: [], refreshTokenIds: []]

        refreshTokenCount.times {
            def clientId = selectRandomClientId(clientIds)

            def refreshToken = createRefreshToken()
            def accessToken = createAccessToken(username, clientId, refreshToken.value)

            tokenIds.accessTokenIds << accessToken.id
            tokenIds.refreshTokenIds << refreshToken.id
        }

        def accessTokenOnlyCount = accessTokenCount - refreshTokenCount

        accessTokenOnlyCount.times {
            def clientId = selectRandomClientId(clientIds)

            def accessToken = createAccessToken(username, clientId)
            tokenIds.accessTokenIds << accessToken.id
        }

        return tokenIds
    }

    private String selectRandomClientId(Collection<String> clientIds) {
        def idx = randomNumberGenerator.nextInt() % clientIds.size()
        return clientIds[idx]
    }

    private static void assertAccessTokensAndRefreshTokensAreDeleted(Map tokenIds) {
        tokenIds.accessTokenIds.each {
            println "Asserting access token [$it] has been deleted"
            assert AccessToken.findById(it) == null
        }
        tokenIds.refreshTokenIds.each {
            println "Asserting refresh token [$it] has been deleted"
            assert RefreshToken.findById(it) == null
        }
    }

    private static Collection<String> createClients(String baseClientId, int count) {
        def clientIds = []

        for(int id = 0; id < count; id++) {
            clientIds << baseClientId + '-' + id
        }

        clientIds.each {
            createClient(it)
        }
        return clientIds
    }

    private static Client createClient(String clientId) {
        new Client(clientId: clientId, clientSecret: 'clientSecret', clientName: 'clientName').save()
    }

    private static User createUser(String username, String clientId) {
        def client = Client.findByClientId(clientId)
        def reel = new Reel(name: Reel.UNCATEGORIZED_REEL_NAME)

        new User(username: username, password: 'secret', displayName: username, email: "$username@test.com")
                .addToClients(client)
                .addToReels(reel)
                .save()
    }

    private AccessToken createAccessToken(String username, String clientId, String refreshTokenValue = null) {
        def tokenValue = 'ACCESS-TOKEN-TEST' + randomNumberGenerator.nextInt()
        println "Creating access token [$tokenValue] for client [$clientId]"

        def authenticationKey = 'authKey' + randomNumberGenerator.nextInt()
        new AccessToken(username: username, clientId: clientId, value: tokenValue, refreshToken: refreshTokenValue,
                tokenType: 'test', scope: ['scope'], expiration: new Date(),
                authenticationKey: authenticationKey, authentication: [1, 2, 3, 4] as byte[]).save()
    }

    private RefreshToken createRefreshToken() {
        def tokenValue = 'REFRESH-TOKEN-TEST' + randomNumberGenerator.nextInt()
        println "Creating refresh token [$tokenValue]"
        new RefreshToken(value: tokenValue, expiration: new Date(), authentication: [5, 6, 7, 8] as byte[]).save()
    }
}
