package test.spec

import grails.test.spock.IntegrationSpec

// All IntegrationSpec tests are executed within a single transaction that is only
// committed or rolled back at the end of the test. In order to test any functionality
// related to email distribution, we must disable the default transactional status and
// directly manage the transaction for each test ourselves.
//
// Any subclass *must* perform any database sanitation required at the conclusion of its tests.
//
// See the following threads for more information:
//
// http://stackoverflow.com/questions/4141601/grails-integration-tests-and-transactions
// http://stackoverflow.com/questions/9866273/grails-transactions-in-integration-tests?lq=1

abstract class MailServiceDependentIntegrationSpec extends IntegrationSpec {

    static transactional = false

    def inMemoryMailService

    void setup() {
        inMemoryMailService.deleteAllMessages()
    }

    void cleanup() {
        inMemoryMailService.deleteAllMessages()
    }
}
