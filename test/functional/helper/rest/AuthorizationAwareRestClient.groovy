package helper.rest

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse

import static helper.rest.HttpContentTypes.MULTI_PART_FORM_DATA
import static helper.rest.HttpHeaders.AUTHORIZATION

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
        def url = attachQueryParams(request.url, request.queryParams)
        restClient."$method"(url) {
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

    private String attachQueryParams(String url, Map<String, Object> queryParams) {
        if(queryParams.isEmpty()) {
            return url
        }
        return url + '?' + queryParams.collect { it }.join('&')
    }
}
