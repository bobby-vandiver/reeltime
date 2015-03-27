package helper.rest

import grails.plugins.rest.client.RestBuilder
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

// Temporary workaround for GRAILS-11073:
// https://jira.grails.org/browse/GRAILS-11073
class PatchedRestBuilder extends RestBuilder {

    @Override
    protected void registerMessageConverters(RestTemplate restTemplate) {
        super.registerMessageConverters(restTemplate)

        final messageConverters = restTemplate.getMessageConverters()
        final mappingJackson2HttpMessageConverter = messageConverters.find { HttpMessageConverter httpMessageConverter -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter }

        if(mappingJackson2HttpMessageConverter) {
            messageConverters.remove(mappingJackson2HttpMessageConverter)
        }

        def bufferedImageConverter = new BufferedImageHttpMessageConverter()
        messageConverters.add(bufferedImageConverter)
    }
}
