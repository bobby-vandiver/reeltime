import grails.util.Environment
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint
import org.springframework.security.web.context.SecurityContextPersistenceFilter

beans = {

    // Entire application is secured by OAuth2
    authenticationEntryPoint(OAuth2AuthenticationEntryPoint)
    accessDeniedHandler(OAuth2AccessDeniedHandler)

    // Ensure all requests are stateless -- nullContextRepository bean is defined by the OAuth2 provider plugin
    securityContextPersistenceFilter(SecurityContextPersistenceFilter, ref('nullContextRepository'))

    // Use AWS backed services by default
    springConfig.addAlias 'storageService', 's3StorageService'
    springConfig.addAlias 'transcoderService', 'elasticTranscoderService'

    Environment.executeForCurrentEnvironment {

        // Use local file system and ffmpeg for local development
        development {
            springConfig.addAlias 'storageService', 'localFileSystemStorageService'
            springConfig.addAlias 'transcoderService', 'ffmpegTranscoderService'
        }

        test {
            springConfig.addAlias 'storageService', 'localFileSystemStorageService'
            springConfig.addAlias 'transcoderService', 'ffmpegTranscoderService'
        }
    }
}
