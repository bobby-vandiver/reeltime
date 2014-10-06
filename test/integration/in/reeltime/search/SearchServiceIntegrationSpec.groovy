package in.reeltime.search

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import test.helper.UserFactory

class SearchServiceIntegrationSpec extends IntegrationSpec {

    def searchService

    void "empty user search result when no users in system"() {
        when:
        def searchResult = searchService.searchForUsers('someone', 1)

        then:
        searchResult.results.size() == 0
    }

    @Unroll
    void "search for user with username [#username] and display name [#display] by query [#query]"() {
        given:
        def user = UserFactory.createUser(username, display)

        when:
        def searchResult = searchService.searchForUsers(query, 1)

        then:
        searchResult.results.contains(user) == shouldContain

        where:
        username    |   display |   query   |   shouldContain
        'joe'       |   null    |   'joe'   |   true
        'joe'       |   null    |   'bill'  |   false
        'foo'       |   'bar'   |   'foo'   |   true
        'foo'       |   'bar'   |   'bar'   |   true
    }
}
