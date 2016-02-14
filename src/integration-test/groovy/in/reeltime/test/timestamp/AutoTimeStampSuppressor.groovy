package in.reeltime.test.timestamp

class AutoTimeStampSuppressor {

    def grailsApplication

    Object withAutoTimestampSuppression(entity, closure) {
        toggleAutoTimestamp(entity, false)
        def result = closure()
        toggleAutoTimestamp(entity, true)
        return result
    }

    private void toggleAutoTimestamp(target, enabled) {
        def applicationContext = grailsApplication.mainContext

        def closureInterceptor = applicationContext.getBean("eventTriggeringInterceptor")
        def datastore = closureInterceptor.datastores.values().iterator().next()
        def interceptor = datastore.getEventTriggeringInterceptor()

        def listener = interceptor.findEventListener(target)
        listener.shouldTimestamp = enabled
    }
}
