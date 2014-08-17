package helper.test

import grails.plugins.rest.client.RestResponse
import helper.rest.AuthorizationAwareRestClient
import helper.rest.RestRequest
import junit.framework.Assert

class ReelTimeClient {

    @Delegate
    private AuthorizationAwareRestClient restClient

    private ReelTimeUrlFactory urlFactory

    ReelTimeClient(AuthorizationAwareRestClient restClient, ReelTimeUrlFactory urlFactory) {
        this.restClient = restClient
        this.urlFactory = urlFactory
    }

    RestResponse registerUser(String name, String pass = 'password', String client = 'client') {
        def url = urlFactory.registerUrl
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
