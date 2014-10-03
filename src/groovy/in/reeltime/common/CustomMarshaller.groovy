package in.reeltime.common

// FIXME: This is a temporary workaround to GRAILS-11116:
// https://jira.grails.org/browse/GRAILS-11116
class CustomMarshaller {

    Object marshall(Object obj) {

        if(obj instanceof Collection) {
            def list = []
            def iter = obj.iterator()

            while(iter.hasNext()) {
                def data = iter.next()
                list << marshall(data)
            }
            return list
        }
        return marshallData(obj)
    }

    private Map marshallData(Object data) {
        if(!data) {
            return [:]
        }

        def clazz = data.class
        if(!hasMarshallerAvailable(clazz)) {
            throw new IllegalArgumentException("Unsupported class [${clazz}]")
        }

        def marshaller = CustomMarshallerRegistrar.marshallers.get(data.class)
        def map = marshaller(data)
        return marshallNestedObjects(map)
    }

    private Map marshallNestedObjects(Map map) {
        def resolvedMap = [:]
        map.each { key, value ->
            if(hasMarshallerAvailable(value.class)) {
                def newValue = marshall(value)
                resolvedMap.put(key, newValue)
            }
            else {
                resolvedMap.put(key, value)
            }
        }
        return resolvedMap
    }

    private boolean hasMarshallerAvailable(Class clazz) {
        return CustomMarshallerRegistrar.marshallers.containsKey(clazz)
    }
}
