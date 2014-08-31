package in.reeltime.maintenance

class ResourceRemovalTarget {

    String uri

    static constraints = {
        uri nullable: false, blank: false, unique: true
    }
}
