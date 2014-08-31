package in.reeltime.maintenance

class ResourceRemovalService {

    void scheduleForRemoval(String uri) {
        new ResourceRemovalTarget(uri: uri).save()
    }
}
