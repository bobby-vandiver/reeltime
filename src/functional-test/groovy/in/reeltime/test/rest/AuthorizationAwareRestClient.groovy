package in.reeltime.test.rest

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse

import static in.reeltime.test.rest.HttpContentTypes.X_WWW_FORM_URL_ENCODED
import static in.reeltime.test.rest.HttpContentTypes.MULTI_PART_FORM_DATA
import static in.reeltime.test.rest.HttpHeaders.AUTHORIZATION
import static in.reeltime.test.rest.HttpHeaders.CONTENT_TYPE

class AuthorizationAwareRestClient {

    private RestBuilder restClient = new BufferedImageCapableRestBuilder()

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
            def headers = request.headers

            if (headers.containsKey(AUTHORIZATION)) {
                header AUTHORIZATION, headers.get(AUTHORIZATION)
            }
            else {
                if (request.token) {
                    header AUTHORIZATION, "Bearer ${request.token}"
                }
            }

            if (headers.containsKey(CONTENT_TYPE)) {
                contentType headers.get(CONTENT_TYPE)
            }
            else {
                if(request.isMultiPart) {
                    contentType MULTI_PART_FORM_DATA
                }
                else {
                    contentType X_WWW_FORM_URL_ENCODED
                }
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
