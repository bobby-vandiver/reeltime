package in.reeltime.deployment.configuration

class GrailsEnvironment {

    static String getGrailsEnv() {
        String env = System.properties['grails.env']
        if (!env) {
            throw new IllegalStateException("Grails environment must be specified via -Dgrails-env")
        }
        return env
    }
}
