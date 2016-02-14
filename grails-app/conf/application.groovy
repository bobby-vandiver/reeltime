final int PRODUCTION_BCRYPT_COST_FACTOR = 16
final int ACCEPTANCE_BCRYPT_COST_FACTOR = 10
final int DEVELOPMENT_BCRYPT_COST_FACTOR = 4
final int TEST_BCRYPT_COST_FACTOR = 4

environments {
    test {
        // Force tests to fail fast and early
        grails.gorm.failOnError = true
    }
}

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'in.reeltime.user.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'in.reeltime.user.UserRole'
grails.plugin.springsecurity.authority.className = 'in.reeltime.user.Role'

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
    [pattern: '/oauth/authorize',      access: "isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
    [pattern: '/oauth/token',          access: "isFullyAuthenticated() and request.getMethod().equals('POST')"]
]

String tokenFilterChain = 'JOINED_FILTERS,-oauth2ProviderFilter,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-exceptionTranslationFilter'
String oauth2ResourceFilterChain = 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-oauth2BasicAuthenticationFilter,-exceptionTranslationFilter'
String awsResourceFilterChain = 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2BasicAuthenticationFilter,-exceptionTranslationFilter'

// TODO: Configure filter chain for admin backend when available
grails.plugin.springsecurity.filterChain.chainMap = [
    [pattern: '/oauth/token',       filters: tokenFilterChain],
    [pattern: '/internal/**',       filters: oauth2ResourceFilterChain],
    [pattern: '/api/**',            filters: oauth2ResourceFilterChain],
    [pattern: '/aws/**',            filters: awsResourceFilterChain],
    [pattern: '/**',                filters: oauth2ResourceFilterChain]
]

// Added by the Spring Security OAuth2 Provider plugin:
grails.plugin.springsecurity.oauthProvider.clientLookup.className = 'in.reeltime.oauth2.Client'
grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.className = 'in.reeltime.oauth2.AuthorizationCode'
grails.plugin.springsecurity.oauthProvider.accessTokenLookup.className = 'in.reeltime.oauth2.AccessToken'
grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.className = 'in.reeltime.oauth2.RefreshToken'

grails.plugin.springsecurity.oauthProvider.realmName = 'ReelTime'

grails.plugin.springsecurity.providerNames = [
        'daoAuthenticationProvider'
]

grails.plugin.springsecurity.password.bcrypt.logrounds = PRODUCTION_BCRYPT_COST_FACTOR

// Spring Security environment specific configuration
environments {
    development {
        grails.plugin.springsecurity.password.bcrypt.logrounds = DEVELOPMENT_BCRYPT_COST_FACTOR
    }
    test {
        grails.plugin.springsecurity.password.bcrypt.logrounds = TEST_BCRYPT_COST_FACTOR
    }
    acceptance {
        grails.plugin.springsecurity.password.bcrypt.logrounds = ACCEPTANCE_BCRYPT_COST_FACTOR
    }
}

// Database migration configuration
environments {
    development {
        grails.plugin.databasemigration.updateOnStart = true
        grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']
    }
    acceptance {
        grails.plugin.databasemigration.updateOnStart = true
        grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']
    }
}

// The following ReelTime settings must NOT be exposed in an external configuration:
reeltime {

    // S3 configuration
    storage {
        // The S3 bucket name where the master video files are stored
        videos = 'master-videos-production'

        // The S3 bucket name where the video segments and playlist are stored
        playlists = 'playlists-and-segments-production'

        // The S3 bucket name where thumbnails are stored
        thumbnails = 'thumbnails-production'

        // Number of times to attempt to generate a unique path before giving up
        pathGenerationMaxRetries = 5
    }

    // Elastic Transcoder configuration
    transcoder {
        // The name of the Elastic Transcoder pipeline to use for transcoding.
        pipeline = 'http-live-streaming-production'

        // The default job input settings to use for all transcoding jobs
        input {
            aspectRatio = 'auto'
            frameRate   = 'auto'
            resolution  = 'auto'
            interlaced  = 'auto'
            container   = 'auto'
        }

        // The settings for transcoding job outputs (segments and playlist)
        output {
            // The length of each video segment in seconds
            segmentDuration = '10'

            // The playlist format -- only HLS version 3 is supported
            format = 'HLSv3'

            // Elastic Transcoder preset Ids:
            // http://docs.aws.amazon.com/elastictranscoder/latest/developerguide/system-presets.html
            presets {
                HLS_400K = '1351620000001-200050'
                HLS_600K = '1351620000001-200040'
                HLS_1M   = '1351620000001-200030'
            }
        }
    }

    // Playlist parser configuration
    playlistParser {
        // Number of times to attempt retrieval of playlist before giving up
        maxRetries = 24

        // Length of time between each attempt
        intervalInMillis = 5000
    }

    // Video metadata configuration
    metadata {
        // Use ffprobe to extract video metadata
        ffprobe = System.getProperty('FFPROBE') ?: System.getenv('FFPROBE')

        // Max video duration is 2 minutes
        maxDurationInSeconds = 2 * 60

        // Max size in bytes of the submitted video stream
        // TODO: Determine average size of 2 minute MP4 video
        maxVideoStreamSizeInBytes = 30 * 1024 * 1024

        // Max size in bytes of the submitted thumbnail video stream
        // TODO: Determine average size of PNG thumbnail
        maxThumbnailStreamSizeInBytes = 30 * 1024 * 1024
    }

    // Account management configuration
    accountManagement {
        // The address to appear in emails
        fromAddress = 'noreply@reeltime.in'

        // How long until an account confirmation code becomes invalid
        confirmationCodeValidityLengthInDays = 7

        // How long until a reset password code becomes invalid
        resetPasswordCodeValidityLengthInMins = 60

        // The BCrypt cost factor to use for storing codes
        bcryptCostFactor = PRODUCTION_BCRYPT_COST_FACTOR
    }

    // User activity configuration
    activity {
        // Max number of activity results per page
        maxActivitiesPerPage = 20
    }

    // General browsing configuration
    browse {
        // Max number of results per page
        maxResultsPerPage = 10
    }

    // Internal maintenance configuration
    maintenance {
        // How many stored resources for the Quartz job to remove per execution
        numberOfResourcesToRemovePerExecution = 1000
    }
}

environments {
    test {
        reeltime {

            storage {
                videos = System.getProperty('java.io.tmpdir') + File.separator + 'master-videos'
                playlists = System.getProperty('java.io.tmpdir') + File.separator + 'playlist-and-segments'
                thumbnails = System.getProperty('java.io.tmpdir') + File.separator + 'thumbnails'
            }

            transcoder {

                ffmpeg {
                    path = System.getProperty('FFMPEG') ?: System.getenv('FFMPEG')
                    segmentFormat = '%s-%%05d.ts'
                }
            }

            accountManagement {
                bcryptCostFactor = TEST_BCRYPT_COST_FACTOR
            }
        }
    }

    development {
        reeltime {

            storage {
                videos = System.getProperty('java.io.tmpdir') + File.separator + 'master-videos'
                playlists = System.getProperty('java.io.tmpdir') + File.separator + 'playlist-and-segments'
                thumbnails = System.getProperty('java.io.tmpdir') + File.separator + 'thumbnails'
            }

            transcoder {

                ffmpeg {
                    path = System.getProperty('FFMPEG') ?: System.getenv('FFMPEG')
                    segmentFormat = '%s-%%05d.ts'
                }
            }

            accountManagement {
                bcryptCostFactor = DEVELOPMENT_BCRYPT_COST_FACTOR
            }
        }
    }

    acceptance {
        reeltime {

            storage {
                videos = 'master-videos-acceptance'
                playlists = 'playlists-and-segments-acceptance'
                thumbnails = 'thumbnails-acceptance'
            }

            transcoder {
                pipeline = 'http-live-streaming-acceptance'
            }

            accountManagement {
                bcryptCostFactor = ACCEPTANCE_BCRYPT_COST_FACTOR
            }
        }
    }
}

// DataSource Configuration

dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""

    if(System.getProperty('ENABLE_SQL_LOGGING') == 'true') {
        logSql = true
    }
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource {
            // TODO: Determine some way to refactor common MySQL configuration
            if(System.getProperty('USE_LOCAL_MYSQL') == 'true') {
                dbCreate = "create"

                username = "root"
                password = "mysql"

                driverClassName = "com.mysql.jdbc.Driver"
                url = "jdbc:mysql://127.0.0.1:33066/test"

                dialect = org.hibernate.dialect.MySQL5InnoDBDialect

                pooled = true
                properties {
                    maxActive = -1
                    minEvictableIdleTimeMillis=1800000
                    timeBetweenEvictionRunsMillis=1800000
                    numTestsPerEvictionRun=3
                    testOnBorrow=true
                    testWhileIdle=true
                    testOnReturn=true
                    validationQuery="SELECT 1"
                }
            }
            else {
                dbCreate = "update"
                url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
            }
        }
    }
    acceptance {
        // TODO: Database connection info should be passed in via environment variables
        dataSource {
            dbCreate = "create"

            username = "acceptance"
            password = "EGQu3kbNqQ2XYrnJ"

            driverClassName = "com.mysql.jdbc.Driver"
            url = "jdbc:mysql://reeltime-acceptance.ck88z1jzf5bj.us-east-1.rds.amazonaws.com:3306/acceptance_database"

            dialect = org.hibernate.dialect.MySQL5InnoDBDialect

            pooled = true
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis=1800000
                timeBetweenEvictionRunsMillis=1800000
                numTestsPerEvictionRun=3
                testOnBorrow=true
                testWhileIdle=true
                testOnReturn=true
                validationQuery="SELECT 1"
            }
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
    }
}

