package helper.rest

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse

import static helper.rest.HttpContentTypes.*
import static helper.rest.HttpHeaders.*

class AuthorizationAwareRestClient {

    private RestBuilder restClient = new PatchedRestBuilder()

    RestResponse get(RestRequest request) {
        doRequest('get', request)
    }

    RestResponse post(RestRequest request) {
        doRequest('post', request)
    }

    RestResponse put(RestRequest request) {
        doRequest('put', request)
    }

    RestResponse delete(RestRequest request) {
        doRequest('delete', request)
    }

    private RestResponse doRequest(String method, RestRequest request) {
        restClient."$method"(request.url) {
            if(request.token) {
                header AUTHORIZATION, "Bearer ${request.token}"
            }
            if(request.isMultiPart) {
                contentType MULTI_PART_FORM_DATA
            }
            if(request.customizer) {
                request.customizer.delegate = delegate
                request.customizer()
            }
        }
    }
}
