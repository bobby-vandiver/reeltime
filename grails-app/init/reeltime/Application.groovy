package reeltime

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration

// TODO: Determine why this is necessary to avoid multiple spring security filter chains being registered
@EnableAutoConfiguration(exclude = [
    org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.ManagementSecurityAutoConfiguration.class])
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}