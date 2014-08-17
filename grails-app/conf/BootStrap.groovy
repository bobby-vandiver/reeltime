import in.reeltime.injection.ConfigInjector

class BootStrap {

    def grailsApplication

    def init = { servletContext ->
        ConfigInjector.injectConfigurableProperties(grailsApplication.config, grailsApplication.mainContext)
    }

    def destroy = {
    }
}
