package in.reeltime.search

import grails.test.spock.IntegrationSpec
import in.reeltime.reel.Audience
import in.reeltime.reel.Reel
import in.reeltime.user.User
import test.helper.UserFactory

class ReelSearchServiceIntegrationSpec extends IntegrationSpec {

    def reelSearchService

    User owner

    void setup() {
        owner = UserFactory.createUser('owner')
    }

    void "include original query in search results"() {
        when:
        def searchResult = reelSearchService.search('test', 1)

        then:
        searchResult.query == 'test'
    }

    void "empty reel search result"() {
        when:
        def searchResult = reelSearchService.search('unknown', 1)

        then:
        searchResult.results.size() == 0
    }

    void "search for uncategorized reel"() {
        when:
        def searchResult = reelSearchService.search('uncategorized', 1)

        then:
        searchResult.results.contains(owner.reels[0])
    }

    void "search for reels by reel name"() {
        given:
        def reel1 = createReel('biggy')
        def reel2 = createReel('small')
        def reel3 = createReel('big tree')

        when:
        def searchResult = reelSearchService.search('small', 1)

        then:
        searchResult.results.contains(reel2)

        and:
        !searchResult.results.contains(reel1)
        !searchResult.results.contains(reel3)
    }

    private Reel createReel(String name) {
        def reel = new Reel(name: name, audience: new Audience())
        owner.addToReels(reel)
        reel.save()
    }
}
