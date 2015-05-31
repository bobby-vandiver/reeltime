package in.reeltime.oauth2

import in.reeltime.user.User

class TokenRemovalService {

    void removeAccessToken(String token) {
        def accessToken = AccessToken.findByValue(token)

        if(accessToken) {
            def refreshToken = RefreshToken.findByValue(accessToken.refreshToken)

            if(refreshToken) {
                log.info "Removing refresh token [${refreshToken.value}]"
                refreshToken.delete()
            }

            log.info "Removing access token [${accessToken.value}]"
            accessToken.delete()
        }
    }

    void removeAllTokensForUser(User user) {
        def clientIds = collectClientIdsForUser(user)
        removeAllTokens(user, clientIds)
    }

    void removeAllTokensForClient(Client client) {
        removeAllTokens(null, [client.clientId])
    }

    private void removeAllTokens(User user, Collection<String> clientIds) {
        def accessTokens = findAccessTokens(user, clientIds)
        def refreshTokens = findRefreshTokensFromAccessTokens(accessTokens)

        accessTokens.each { AccessToken accessToken ->
            log.info "Removing access token [${accessToken.id}]"
            accessToken.delete()
        }

        refreshTokens.each { RefreshToken refreshToken ->
            log.info "Removing refresh token [${refreshToken.id}]"
            refreshToken.delete()
        }
    }

    private static Collection<String> collectClientIdsForUser(User user) {
        user.clients.collect { it.clientId }
    }

    private static Collection<AccessToken> findAccessTokens(User user, Collection<String> clientIds) {
        def userTokens = user ? AccessToken.findAll { username == user.username } : []
        def clientTokens = AccessToken.findAll { clientIds.contains(clientId) }
        return userTokens + clientTokens
    }

    private static Collection<RefreshToken> findRefreshTokensFromAccessTokens(Collection<AccessToken> accessTokens) {
        def refreshTokenValues = collectRefreshTokenValuesFromAccessTokens(accessTokens)

        RefreshToken.findAll {
            refreshTokenValues.contains(value)
        }
    }

    private static Collection<String> collectRefreshTokenValuesFromAccessTokens(Collection<AccessToken> accessTokens) {
        def refreshTokenValues = []
        accessTokens.each {
            def refreshToken = it.refreshToken

            if(refreshToken) {
                refreshTokenValues << refreshToken
            }
        }
        return refreshTokenValues
    }
}
