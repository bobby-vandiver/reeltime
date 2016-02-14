package in.reeltime.search

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class PagedListCommandSpec extends Specification {

    @Unroll
    void "page [#page] is valid [#valid]"() {
        given:
        def command = new PagedListCommand(page: page)

        expect:
        command.validate(['page']) == valid

        and:
        command.errors.getFieldError('page')?.code == code

        where:
        page        |   valid   |   code
        -1          |   false   |   'min.notmet'
        0           |   false   |   'min.notmet'
        1           |   true    |   null
        42          |   true    |   null
    }

    void "request first page if page not specified"() {
        given:
        def command = new PagedListCommand(page: null)

        expect:
        command.validate(['validate'])

        and:
        command.page == 1
    }
}
