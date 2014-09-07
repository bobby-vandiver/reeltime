package in.reeltime.maintenance

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.storage.StorageService
import spock.lang.Specification

@TestFor(ResourceRemovalService)
@Mock([ResourceRemovalTarget])
class ResourceRemovalServiceSpec extends Specification {

    StorageService storageService

    private static final BASE = 'test'
    private static final RELATIVE_PREFIX = 'file-'

    void setup() {
        storageService = Mock(StorageService)
        service.storageService = storageService
    }

    void "delete the oldest targets first"() {
        given:
        def targets = createTargets(10)

        def targetIdsToDelete = targets[0..4].collect { it.id }
        def targetIdsToKeep = targets[5..9].collect { it.id }

        assert targetIdsToDelete.size() == 5
        assert targetIdsToKeep.size() == 5

        when:
        service.executeScheduledRemovals(5)

        then:
        assertExistenceOfTargets(targetIdsToDelete, false)
        assertExistenceOfTargets(targetIdsToKeep, true)

        and:
        5 * storageService.delete(*_) >> { args ->
            assert args[0] == BASE

            def target = ResourceRemovalTarget.findByRelative(args[1])
            assert targetIdsToDelete.contains(target.id)
        }
    }

    private Collection<ResourceRemovalTarget> createTargets(int count) {
        Collection<ResourceRemovalTarget> targets = []

        for(int i = 0; i < count; i++) {
            def target = new ResourceRemovalTarget(base: BASE, relative: RELATIVE_PREFIX + i).save(flush: true)
            assert target.dateCreated != null

            targets << target
            sleep(500)
        }
        targets.sort { a, b -> a.dateCreated <=> b.dateCreated }
        return targets
    }

    private assertExistenceOfTargets(Collection<Long> targetIds, boolean shouldExist) {
        targetIds.each { id ->
            boolean exists = (ResourceRemovalTarget.findById(id) != null)
            assert exists == shouldExist
        }
    }
}
