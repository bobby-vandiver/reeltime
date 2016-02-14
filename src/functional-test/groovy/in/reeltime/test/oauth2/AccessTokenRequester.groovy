package in.reeltime.test.oauth2

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import static in.reeltime.test.config.EnvironmentConfiguration.baseUrl

class AccessTokenRequester {

    private static RESTClient restClient = new RESTClient()

    static final String TOKEN_ENDPOINT_URL = baseUrl + 'oauth/token'

    static HttpResponseDecorator requestAccessToken(Map params) {
        try {
            restClient.post(uri: TOKEN_ENDPOINT_URL, query: params) as HttpResponseDecorator
        }
        catch(HttpResponseException e) {
            return e.response
        }
    }

    static String getAccessToken(Map params) {
        def response = requestAccessToken(params)
        return response.data.access_token
    }

    static String getRefreshToken(Map params) {
        def response = requestAccessToken(params)
        return response.data.refresh_token
    }

    static HttpResponseDecorator requestAccessTokenWithBasicAuth(Map params, String clientId, String clientSecret) {
        def basicAuth = "$clientId:$clientSecret".bytes.encodeBase64()
        def headers = [Authorization: "Basic $basicAuth"]
        restClient.post(uri: TOKEN_ENDPOINT_URL, query: params, headers: headers) as HttpResponseDecorator
    }
}