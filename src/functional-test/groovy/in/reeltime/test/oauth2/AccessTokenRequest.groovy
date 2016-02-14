package in.reeltime.test.oauth2

class AccessTokenRequest {
    String grantType

    String clientId
    String clientSecret

    String username
    String password

    Collection<String> scope

    Map getParams() {
        [
                client_id: clientId,
                client_secret: clientSecret,
                grant_type: grantType,
                username: username,
                password: password,
                scope: convertScopeToSpaceDelimitedString()
        ]
    }

    private String convertScopeToSpaceDelimitedString() {
        def scopes = ''
        for (int i = 0; i < scope.size(); i++) {
            scopes += scope.getAt(i)
            if (i + 1 < scope.size()) {
                scopes += ' '
            }
        }
        return scopes
    }
}