import grails.util.Environment
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint

beans = {

    // Entire application is secured by OAuth2
    authenticationEntryPoint(OAuth2AuthenticationEntryPoint) {
        realmName = 'ReelTime'
    }
    accessDeniedHandler(OAuth2AccessDeniedHandler)

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
