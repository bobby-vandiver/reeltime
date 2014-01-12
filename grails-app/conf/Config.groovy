// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
    test {
        // Force tests to fail fast and early
        grails.gorm.failOnError = true
    }
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

log4j = {

    debug  'in.reeltime',
           'grails.app'

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'in.reeltime.user.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'in.reeltime.user.UserRole'
grails.plugin.springsecurity.authority.className = 'in.reeltime.user.Role'

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
    // TODO: Temporarily allow access to all endpoints for development
    '/**':                            ['permitAll'],

	'/':                              ['permitAll'],
	'/index':                         ['permitAll'],
	'/index.gsp':                     ['permitAll'],
	'/**/js/**':                      ['permitAll'],
	'/**/css/**':                     ['permitAll'],
	'/**/images/**':                  ['permitAll'],
	'/**/favicon.ico':                ['permitAll']
]

// The following ReelTime settings must NOT be exposed in an external configuration:
reeltime {

    // S3 configuration
    storage {
        // The S3 bucket name where the master video files are stored
        input = 'master-videos-test'

        // The S3 bucket name where the video segments and playlist are stored
        output = 'playlist-and-segments-test'
    }

    // Elastic Transcoder configuration
    transcoder {
        // The name of the Elastic Transcoder pipeline to use for transcoding.
        pipeline = 'http-live-streaming-test'

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
}