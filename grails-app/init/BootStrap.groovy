import grails.core.GrailsApplication
import in.reeltime.injection.ConfigInjector

class BootStrap {

    GrailsApplication grailsApplication

    def init = { servletContext ->
        ConfigInjector.injectConfigurableProperties(grailsApplication.config, grailsApplication.mainContext)
    }

    def destroy = {
    }
}
