package in.reeltime.injection

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.maintenance.ResourceRemovalJob
import in.reeltime.video.VideoCreationCommand
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
@Ignore("Need to determine if there's a better way to enforce read only state that doesn't interfere with tests")
class ConfigInjectorIntegrationSpec extends Specification {

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
