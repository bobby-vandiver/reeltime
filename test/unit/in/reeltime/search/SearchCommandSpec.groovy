package in.reeltime.search

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class SearchCommandSpec extends Specification {

    @Unroll
    void "key [#key] with value [#value] is valid [#valid]"() {
        given:
        def command = new SearchCommand((key): value)

        expect:
        command.validate([key]) == valid

        and:
        command.errors.getFieldError(key)?.code == code

        where:
        key     |   value       |   valid   |   code
        'type'  |   null        |   false   |   'nullable'
        'type'  |   ''          |   false   |   'blank'
        'type'  |   'client'    |   false   |   'not.inList'
        'type'  |   'user'      |   true    |   null
        'type'  |   'video'     |   true    |   null
        'type'  |   'reel'      |   true    |   null

        'query' |   null        |   false   |   'nullable'
        'query' |   ''          |   false   |   'blank'
        'query' |   'some word' |   true    |   null

        'page'  |   null        |   false   |   'nullable'
        'page'  |   -1          |   false   |   'min.notmet'
        'page'  |   0           |   false   |   'min.notmet'
        'page'  |   1           |   true    |   null
        'page'  |   42          |   true    |   null
    }

    void "request first page if page not specified"() {
        given:
        def command = new SearchCommand()

        expect:
        command.page == 1
        command.validate(['page'])
    }
}
