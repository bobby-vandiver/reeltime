package helper.test

import grails.plugins.rest.client.RestResponse
import helper.rest.AuthorizationAwareRestClient
import helper.rest.RestRequest
import junit.framework.Assert

class ReelTimeClient {

    @Delegate
    private AuthorizationAwareRestClient restClient

    private String baseUrl

    ReelTimeClient(AuthorizationAwareRestClient restClient, String baseUrl) {
        this.restClient = restClient
        this.baseUrl = baseUrl
    }

    String getUrlForResource(String resource) {
        return baseUrl + resource
    }

    RestResponse registerUser(String name, String pass = 'password', String client = 'client') {
        def url = getUrlForResource('account/register')
        def request = new RestRequest(url: url, customizer: {
            email = name + '@test.com'
            username = name
            password = pass
            client_name = client
        })
        def response = post(request)
        if(response.status != 201) {
            Assert.fail("Failed to register user. Status code: ${response.status}. JSON: ${response.json}")
        }
        return response
    }

}
