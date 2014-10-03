package in.reeltime.common

// FIXME: This is a temporary workaround to GRAILS-11116:
// https://jira.grails.org/browse/GRAILS-11116
class CustomMarshaller {

    private CustomMarshallerRegistrar registrar = new CustomMarshallerRegistrar()

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
        if(!registrar.hasMarshallerAvailable(clazz)) {
            throw new IllegalArgumentException("Unsupported class [${clazz}]")
        }

        def marshaller = registrar.getMarshaller(clazz)
        def map = marshaller(data)
        return marshallNestedObjects(map)
    }

    private Map marshallNestedObjects(Map map) {
        def resolvedMap = [:]
        map.each { key, value ->
            if(registrar.hasMarshallerAvailable(value.class)) {
                def newValue = marshall(value)
                resolvedMap.put(key, newValue)
            }
            else {
                resolvedMap.put(key, value)
            }
        }
        return resolvedMap
    }
}
