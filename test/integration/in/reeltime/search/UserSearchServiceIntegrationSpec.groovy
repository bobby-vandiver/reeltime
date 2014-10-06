package in.reeltime.search

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import test.helper.UserFactory

class UserSearchServiceIntegrationSpec extends IntegrationSpec {

    def userSearchService

    void "include original query in search results"() {
        when:
        def searchResult = userSearchService.search('test', 1)

        then:
        searchResult.query == 'test'
    }

    void "empty user search result when no users in system"() {
        when:
        def searchResult = userSearchService.search('someone', 1)

        then:
        searchResult.results.size() == 0
    }

    @Unroll
    void "search for user with username [#username] and display name [#display] by query [#query]"() {
        given:
        def user = UserFactory.createUser(username, display)

        when:
        def searchResult = userSearchService.search(query, 1)

        then:
        searchResult.results.contains(user) == shouldContain

        where:
        username    |   display |   query   |   shouldContain
        'joe'       |   null    |   'joe'   |   true
        'joe'       |   null    |   'jo'    |   true
        'joe'       |   null    |   'oe'    |   true
        'joe'       |   null    |   'bill'  |   false
        'foo'       |   'bar'   |   'foo'   |   true
        'foo'       |   'bar'   |   'bar'   |   true
    }

    void "partial matches should appear lower in results"() {
        given:
        def joe = UserFactory.createUser('joe')
        def joey = UserFactory.createUser('joey')
        def joel = UserFactory.createUser('joel')

        when:
        def searchResult = userSearchService.search('joe', 1)

        then:
        searchResult.results[0] == joe
        searchResult.results[1] == joey
        searchResult.results[2] == joel
    }

    void "search results match username and display name regardless of case"() {
        given:
        def bob = UserFactory.createUser('BOB')
        def joe = UserFactory.createUser('joe', 'joe bOb')
        def jay = UserFactory.createUser('jay', 'Silent Bob')
        def sally = UserFactory.createUser('sally')

        when:
        def searchResult = userSearchService.search('bob', 1)

        then:
        searchResult.results.contains(bob)
        searchResult.results.contains(joe)
        searchResult.results.contains(jay)

        and:
        !searchResult.results.contains(sally)
    }
}
