package in.reeltime.common

// FIXME: This is a temporary workaround to GRAILS-11116:
// https://jira.grails.org/browse/GRAILS-11116
class CustomMarshaller {

    private CustomMarshallerRegistry registry = new CustomMarshallerRegistry()

    Object marshall(Object obj) {
        def clazz = obj?.class

        if(obj instanceof Collection) {
            def list = []
            def iter = obj.iterator()

            while(iter.hasNext()) {
                def data = iter.next()
                list << marshall(data)
            }
            return list
        }
        else if(obj instanceof Map) {
            def map = [:]
            obj.each { key, value ->
                map.put(key, marshall(value))
            }
            return map
        }
        else if(registry.hasMarshallerAvailable(clazz)) {
            def marshaller = registry.getMarshaller(clazz)
            def data = marshaller(obj)
            return marshall(data)
        }
        else {
            return obj
        }
    }
}
