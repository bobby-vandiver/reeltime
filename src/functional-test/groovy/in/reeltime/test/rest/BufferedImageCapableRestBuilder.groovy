package in.reeltime.test.rest

import grails.plugins.rest.client.RestBuilder
import groovy.transform.CompileStatic
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.web.client.RestTemplate

@CompileStatic
class BufferedImageCapableRestBuilder extends RestBuilder {

    @Override
    protected void registerMessageConverters(RestTemplate restTemplate) {
        super.registerMessageConverters(restTemplate)

        final messageConverters = restTemplate.getMessageConverters()

        def bufferedImageConverter = new BufferedImageHttpMessageConverter()
        messageConverters.add(bufferedImageConverter)
    }
}
