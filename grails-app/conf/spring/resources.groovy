import grails.util.Environment

beans = {

    Environment.executeForCurrentEnvironment {

        // Use local file system and ffmpeg unless in production on AWS
        springConfig.addAlias 'storageService', 'localFileSystemStorage'
        springConfig.addAlias 'transcoderService', 'ffmpegTranscoderService'

        production {
            springConfig.addAlias 'storageService', 's3StorageService'
            springConfig.addAlias 'transcoderService', 'elasticTranscoderService'
        }
    }
}
