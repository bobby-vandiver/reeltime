// TODO: Convert to YAML and merge with application.yml

final int PRODUCTION_BCRYPT_COST_FACTOR = 16
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

grails.plugin.springsecurity.password.bcrypt.logrounds =
        Integer.getInteger("BCRYPT_COST_FACTOR", PRODUCTION_BCRYPT_COST_FACTOR)

// Spring Security environment specific configuration
environments {
    development {
        grails.plugin.springsecurity.password.bcrypt.logrounds = DEVELOPMENT_BCRYPT_COST_FACTOR
    }
    test {
        grails.plugin.springsecurity.password.bcrypt.logrounds = TEST_BCRYPT_COST_FACTOR
    }
}

// Database migration configuration
grails.plugin.databasemigration.dropOnStart = false
grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

environments {
    development {
        grails.plugin.databasemigration.dropOnStart = true
    }
    test {
        grails.plugin.databasemigration.dropOnStart = true
    }
}


// The following ReelTime settings must NOT be exposed in an external configuration:
reeltime {

    // S3 configuration
    storage {
        // The S3 bucket name where the master video files are stored
        videos = System.getProperty("MASTER_VIDEOS_BUCKET_NAME")

        // The S3 bucket name where the video segments and playlist are stored
        playlists = System.getProperty("PLAYLISTS_AND_SEGMENTS_BUCKET_NAME")

        // The S3 bucket name where thumbnails are stored
        thumbnails = System.getProperty("THUMBNAILS_BUCKET_NAME")

        // Number of times to attempt to generate a unique path before giving up
        pathGenerationMaxRetries = 5
    }

    // Elastic Transcoder configuration
    transcoder {
        // The name of the Elastic Transcoder pipeline to use for transcoding.
        pipeline = System.getProperty("TRANSCODER_PIPELINE_NAME")

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
        bcryptCostFactor = Integer.getInteger("BCRYPT_COST_FACTOR", PRODUCTION_BCRYPT_COST_FACTOR)

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

    // Email configuration
    email {
        mailgun {
            // Base URL for the Mailgun API
            baseUrl = 'https://api.mailgun.net/v3'

            // The domain registered with Mailgun
            domainName = 'reeltime.in'

            // Mailgun API key
            apiKey = System.getProperty('MAILGUN_API_KEY') ?: System.getenv('MAILGUN_API_KEY')
        }
    }

    // Internal maintenance configuration
    maintenance {
        // How many stored resources for the Quartz job to remove per execution
        numberOfResourcesToRemovePerExecution = 1000
    }
}

final String LOCAL_MASTER_VIDEOS_DIR = System.getProperty('java.io.tmpdir') + File.separator + 'master-videos'
final String LOCAL_PLAYLISTS_AND_SEGMENTS_DIR = System.getProperty('java.io.tmpdir') + File.separator + 'playlist-and-segments'
final String LOCAL_THUMBNAILS_DIR = System.getProperty('java.io.tmpdir') + File.separator + 'thumbnails'

final String FFMPEG_PATH = System.getProperty('FFMPEG') ?: System.getenv('FFMPEG')
final String FFMPEG_SEGMENT_FORMAT = '%s-%%05d.ts'

environments {
    test {
        reeltime {
            storage {
                videos = LOCAL_MASTER_VIDEOS_DIR
                playlists = LOCAL_PLAYLISTS_AND_SEGMENTS_DIR
                thumbnails = LOCAL_THUMBNAILS_DIR
            }

            transcoder {
                ffmpeg {
                    path = FFMPEG_PATH
                    segmentFormat = FFMPEG_SEGMENT_FORMAT
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
                videos = LOCAL_MASTER_VIDEOS_DIR
                playlists = LOCAL_PLAYLISTS_AND_SEGMENTS_DIR
                thumbnails = LOCAL_THUMBNAILS_DIR
            }

            transcoder {
                ffmpeg {
                    path = FFMPEG_PATH
                    segmentFormat = FFMPEG_SEGMENT_FORMAT
                }
            }

            accountManagement {
                bcryptCostFactor = DEVELOPMENT_BCRYPT_COST_FACTOR
            }
        }
    }
}

// DataSource Configuration

dataSource {
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

environments {
    development {
        dataSource {
            if(Boolean.getBoolean('USE_LOCAL_MYSQL')) {
                username = "root"
                password = "mysql"

                url = "jdbc:mysql://127.0.0.1:3306/reeltime"

                driverClassName = "com.mysql.jdbc.Driver"
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
                url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
            }
        }
    }
    test {
        dataSource {
            if(Boolean.getBoolean('USE_LOCAL_MYSQL')) {
                username = "root"
                password = "mysql"

                url = "jdbc:mysql://127.0.0.1:3306/reeltime"

                driverClassName = "com.mysql.jdbc.Driver"
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
                url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
            }
        }
    }
    production {
        dataSource {
            username = System.getProperty("DATABASE_USERNAME")
            password = System.getProperty("DATABASE_PASSWORD")

            url = System.getProperty("JDBC_CONNECTION_STRING")

            driverClassName = "com.mysql.jdbc.Driver"
            dialect = org.hibernate.dialect.MySQL5InnoDBDialect

            pooled = true

            // TODO: Determine what properties need to be set for MySQL
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

