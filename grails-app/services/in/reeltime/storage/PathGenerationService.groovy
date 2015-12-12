package in.reeltime.storage

import grails.transaction.Transactional

@Transactional
class PathGenerationService {

    def storageService

    def maxRetries

    String generateRandomUniquePath(base) {
        def path = randomUUIDString()

        int attempt = 0
        boolean exists = storageService.exists(base, path)

        while(exists && attempt < maxRetries) {
            path = randomUUIDString()
            exists = storageService.exists(base, path)
            attempt++
        }

        if(exists) {
            throw new IllegalStateException("Path generation for base [$base] exceeded max retries")
        }

        return path
    }

    private static String randomUUIDString() {
        UUID.randomUUID() as String
    }
}
