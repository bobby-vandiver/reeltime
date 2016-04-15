package in.reeltime.maintenance

import grails.transaction.Transactional

@Transactional
class ResourceRemovalService {

    def storageService

    void scheduleForRemoval(String base, String relative) {
        log.info "Scheduling [$base :: $relative] for removal"
        new ResourceRemovalTarget(base: base, relative: relative).save()
    }

    void executeScheduledRemovals(int numberToRemove) {
        def list = ResourceRemovalTarget.list(max: numberToRemove, sort: 'dateCreated', order: 'asc')
        list.each { target ->
            log.info "Removing target [${target.id}]"
            storageService.delete(target.base, target.relative)
            target.delete()
        }
    }
}
