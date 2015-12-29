package in.reeltime.injection

import grails.test.spock.IntegrationSpec
import in.reeltime.maintenance.ResourceRemovalJob
import in.reeltime.video.VideoCreationCommand
import spock.lang.Unroll

class ConfigInjectorIntegrationSpec extends IntegrationSpec {

    @Unroll
    void "cannot set readonly static [#propertyName] for class [#clazz]"() {
        when:
        clazz."$propertyName" = propertyValue

        then:
        thrown(ReadOnlyPropertyException)

        where:
        clazz                   |   propertyName                    |   propertyValue
        VideoCreationCommand    |   'maxDuration'                   |   1234
        ResourceRemovalJob      |   'numberToRemovePerExecution'    |   5678
    }
}
