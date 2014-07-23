import grails.util.Environment
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint

beans = {

    // Entire application is secured by OAuth2
    authenticationEntryPoint(OAuth2AuthenticationEntryPoint) {
        realmName = 'ReelTime'
    }
    accessDeniedHandler(OAuth2AccessDeniedHandler)

    String environmentName = Environment.currentEnvironment.name
    switch(environmentName) {

        // Use AWS backed services for production and acceptance
        case 'production':
        case 'acceptance':
            configureAwsBeans.delegate = delegate
            configureAwsBeans ()
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

configureAwsBeans = {
    springConfig.addAlias 'storageService', 's3StorageService'
    springConfig.addAlias 'transcoderService', 'elasticTranscoderService'
    springConfig.addAlias 'mailService', 'simpleEmailMailService'
}

configureLocalBeans = {
    springConfig.addAlias 'storageService', 'localFileSystemStorageService'
    springConfig.addAlias 'transcoderService', 'ffmpegTranscoderService'
    springConfig.addAlias 'mailService', 'inMemoryMailService'
}