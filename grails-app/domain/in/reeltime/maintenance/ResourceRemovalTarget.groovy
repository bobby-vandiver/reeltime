package in.reeltime.maintenance

import groovy.transform.ToString

@ToString(includeNames = true)
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
