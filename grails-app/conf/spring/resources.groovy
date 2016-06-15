import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import in.reeltime.mail.EmailManager
import in.reeltime.springsecurity.PatchedAnnotationFilterInvocationDefinition

beans = {
    emailManager(EmailManager) {
        mailService = ref('mailService')
        mailServerExistenceService = ref('mailServerExistenceService')
    }

    configureSpringSecurityCorePluginOverrides.delegate = delegate
    configureSpringSecurityCorePluginOverrides()

    String environmentName = Environment.currentEnvironment.name
    switch(environmentName) {

        // Use cloud based services for production
        case 'production':
            configureCloudBeans.delegate = delegate
            configureCloudBeans ()
            break

        // Use local file system, ffmpeg and in-memory implementations for local development and testing
        case 'development':
        case 'test':
            configureLocalBeans.delegate = delegate
            configureLocalBeans ()
            break

        default:
            throw new IllegalStateException("Unknown environment ${environmentName}")
    }
}

configureCloudBeans = {
    springConfig.addAlias 'storageService', 's3StorageService'
    springConfig.addAlias 'transcoderService', 'elasticTranscoderService'
    springConfig.addAlias 'mailService', 'mailgunService'
    springConfig.addAlias 'mailServerExistenceService', 'MXRecordMailServerExistenceService'
}

configureLocalBeans = {
    springConfig.addAlias 'storageService', 'localFileSystemStorageService'
    springConfig.addAlias 'transcoderService', 'ffmpegTranscoderService'
    springConfig.addAlias 'mailService', 'localMailService'
    springConfig.addAlias 'mailServerExistenceService', 'localMailServerExistenceService'
}

configureSpringSecurityCorePluginOverrides = {
    def conf = SpringSecurityUtils.securityConfig

    // Overriding this bean provided by Spring Security Core to disable the
    // adding an intercept url rule for the defaultAction in an annotated controller.

    objectDefinitionSource(PatchedAnnotationFilterInvocationDefinition) {
        application = ref('grailsApplication')
        grailsUrlConverter = ref('grailsUrlConverter')
        httpServletResponseExtension = ref('httpServletResponseExtension')
        if (conf.rejectIfNoRule instanceof Boolean) {
            rejectIfNoRule = conf.rejectIfNoRule
        }
    }
}