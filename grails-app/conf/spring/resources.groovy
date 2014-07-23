import grails.util.Environment
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint

beans = {

    // Entire application is secured by OAuth2
    authenticationEntryPoint(OAuth2AuthenticationEntryPoint) {
        realmName = 'ReelTime'
    }
    accessDeniedHandler(OAuth2AccessDeniedHandler)

    Environment.executeForCurrentEnvironment {

        // Use AWS backed services for production
        production {
            configureAwsBeans.delegate = delegate
            configureAwsBeans()
        }

        // Use local file system, ffmpeg and in-memory implementations for local development and testing
        development {
            configureLocalBeans.delegate = delegate
            configureLocalBeans()
        }

        test {
            configureLocalBeans.delegate = delegate
            configureLocalBeans()
        }
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