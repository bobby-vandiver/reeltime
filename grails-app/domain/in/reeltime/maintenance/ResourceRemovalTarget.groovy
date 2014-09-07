package in.reeltime.maintenance

class ResourceRemovalTarget {

    Date dateCreated

    String base
    String relative

    static constraints = {
        base nullable: false, blank: false
        relative nullable: false, blank: false, validator: { val, obj ->
            ResourceRemovalTarget.findByBaseAndRelative(obj.base, obj.relative) == null
        }
    }
}
