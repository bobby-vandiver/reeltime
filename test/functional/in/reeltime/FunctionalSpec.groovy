package in.reeltime

import grails.plugins.rest.client.RestBuilder
import grails.util.BuildSettings
import helper.oauth2.AccessTokenRequester
import helper.oauth2.RESTClientWrapper
import spock.lang.Specification

abstract class FunctionalSpec extends Specification {

    protected static final BASE_URL = System.getProperty(BuildSettings.FUNCTIONAL_BASE_URL_PROPERTY)
    protected static RESTClientWrapper restClient = new RESTClientWrapper(BASE_URL)

    // TODO: Specify user once user registration is implemented
    protected static String getAccessTokenWithScope(String scope) {
        def params = [
                client_id: 'test-client',
                client_secret: 'test-secret',
                grant_type: 'password',
                username: 'bob',
                password: 'pass',
                scope: scope
        ]
        AccessTokenRequester.getAccessToken(params)
    }
}
