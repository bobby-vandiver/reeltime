package helper.oauth2

import groovy.transform.InheritConstructors
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.client.ClientProtocolException

@InheritConstructors
class RESTClientWrapper extends RESTClient {

    @Override
    Object post(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
        handleRequest {
            super.post(args)
        }
    }

    @Override
    Object get(Map<String, ?> args) throws ClientProtocolException, IOException, URISyntaxException {
        handleRequest {
            super.get(args)
        }
    }

    private static Object handleRequest(Closure handler) {
        try {
            handler()
        }
        catch(HttpResponseException e) {
            return e.response
        }
    }
}
