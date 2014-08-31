package in.reeltime.maintenance

class ResourceRemovalService {

    void scheduleForRemoval(String base, String relative) {
        log.info "Scheduling [$base :: $relative] for removal"
        new ResourceRemovalTarget(base: base, relative: relative).save()
    }
}
