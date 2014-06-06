package in.reeltime.metadata

import grails.test.spock.IntegrationSpec

class StreamMetadataIntegrationSpec extends IntegrationSpec {

    void "cannot set readonly static maxDuration"() {
        when:
        StreamMetadata.maxDuration = 1234

        then:
        thrown(ReadOnlyPropertyException)
    }
}
