import in.reeltime.injection.ConfigInjector
import org.quartz.impl.StdScheduler

class BootStrap {

    def grailsApplication

    def init = { servletContext ->
        ConfigInjector.injectConfigurableProperties(grailsApplication.config, grailsApplication.mainContext)

        def quartzScheduler = grailsApplication.mainContext.getBean('quartzScheduler') as StdScheduler
        if(!quartzScheduler.started) {
            log.info "Starting Quartz scheduler"
            quartzScheduler.start()
        }
    }

    def destroy = {
    }
}
