import grails.util.Environment

beans = {

    // Use local file system and ffmpeg unless in production on AWS
    springConfig.addAlias 'storageService', 'localFileSystemStorageService'
    springConfig.addAlias 'transcoderService', 'ffmpegTranscoderService'

    Environment.executeForCurrentEnvironment {
        production {
            springConfig.addAlias 'storageService', 's3StorageService'
            springConfig.addAlias 'transcoderService', 'elasticTranscoderService'
        }
    }
}
