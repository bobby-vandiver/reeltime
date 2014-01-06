import grails.util.Environment

beans = {

    Environment.executeForCurrentEnvironment {

        test {
            springConfig.addAlias 'storageService', 'localFileSystemStorage'
        }

        development {
            springConfig.addAlias 'storageService', 'localFileSystemStorage'
        }

        production {
            springConfig.addAlias 'storageService', 's3StorageService'
        }
    }
}
