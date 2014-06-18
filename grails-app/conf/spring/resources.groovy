import grails.util.Environment
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import in.reeltime.security.JsonAccessDeniedHandler

beans = {

    // Returns a 403 status code and JSON response containing the error
    accessDeniedHandler(JsonAccessDeniedHandler)

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
