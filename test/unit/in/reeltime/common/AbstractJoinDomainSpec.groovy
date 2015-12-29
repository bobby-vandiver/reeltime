package in.reeltime.common

import grails.plugin.springsecurity.SpringSecurityService
import spock.lang.Specification
import in.reeltime.user.User
import spock.lang.Unroll

abstract class AbstractJoinDomainSpec extends Specification {

    abstract Class getJoinClass()

    abstract Class getLeftPropertyClass()

    abstract Class getRightPropertyClass()

    abstract String getLeftPropertyName()

    abstract String getRightPropertyName()

    @Unroll
    void "same #l and same #r"() {
        given:
        def leftProperty = createDomainInstance(leftPropertyClass)
        def rightProperty = createDomainInstance(rightPropertyClass)

        and:
        def lhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty, (rightPropertyName): rightProperty])
        def rhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty, (rightPropertyName): rightProperty])

        expect:
        lhs.equals(rhs)
        rhs.equals(rhs)

        and:
        lhs.hashCode() == rhs.hashCode()

        where:
        l = leftPropertyName
        r = rightPropertyName
    }

    @Unroll
    void "different #l and same #r"() {
        given:
        def leftProperty1 = createDomainInstance(leftPropertyClass)
        def leftProperty2 = createDomainInstance(leftPropertyClass)

        def rightProperty = createDomainInstance(rightPropertyClass)

        and:
        def lhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty1, (rightPropertyName): rightProperty])
        def rhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty2, (rightPropertyName): rightProperty])

        expect:
        leftProperty1.equals(leftProperty2)
        !leftProperty1.id.equals(leftProperty2.id)

        and:
        !lhs.equals(rhs)
        !rhs.equals(lhs)

        and:
        lhs.hashCode() != rhs.hashCode()

        where:
        l = leftPropertyName
        r = rightPropertyName
    }

    @Unroll
    void "same #l and different #r"() {
        given:
        def leftProperty = createDomainInstance(leftPropertyClass)

        def rightProperty1 = createDomainInstance(rightPropertyClass)
        def rightProperty2 = createDomainInstance(rightPropertyClass)

        and:
        def lhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty, (rightPropertyName): rightProperty1])
        def rhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty, (rightPropertyName): rightProperty2])

        expect:
        rightProperty1.equals(rightProperty2)
        !rightProperty1.id.equals(rightProperty2.id)

        and:
        !lhs.equals(rhs)
        !rhs.equals(lhs)

        and:
        lhs.hashCode() != rhs.hashCode()

        where:
        l = leftPropertyName
        r = rightPropertyName
    }

    @Unroll
    void "null #l and same #r"() {
        given:
        def rightProperty = createDomainInstance(rightPropertyClass)

        and:
        def lhs = createDomainInstance(joinClass, [(leftPropertyName): null, (rightPropertyName): rightProperty])
        def rhs = createDomainInstance(joinClass, [(leftPropertyName): null, (rightPropertyName): rightProperty])

        expect:
        lhs.equals(rhs)
        rhs.equals(rhs)

        and:
        lhs.hashCode() == rhs.hashCode()

        where:
        l = leftPropertyName
        r = rightPropertyName
    }

    @Unroll
    void "same #l and null #r"() {
        given:
        def leftProperty = createDomainInstance(leftPropertyClass)

        and:
        def lhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty, (rightPropertyName): null])
        def rhs = createDomainInstance(joinClass, [(leftPropertyName): leftProperty, (rightPropertyName): null])

        expect:
        lhs.equals(rhs)
        rhs.equals(rhs)

        and:
        lhs.hashCode() == rhs.hashCode()

        where:
        l = leftPropertyName
        r = rightPropertyName
    }

    @Unroll
    void "null #l and null #r"() {
        given:
        def lhs = createDomainInstance(joinClass, [(leftPropertyName): null, (rightPropertyName): null])
        def rhs = createDomainInstance(joinClass, [(leftPropertyName): null, (rightPropertyName): null])

        expect:
        lhs.equals(rhs)
        rhs.equals(rhs)

        and:
        lhs.hashCode() == rhs.hashCode()

        where:
        l = leftPropertyName
        r = rightPropertyName
    }

    private Object createDomainInstance(Class domainClazz, Map args = [:]) {
        def obj = domainClazz.newInstance(args)

        if (domainClazz == User) {
            obj.springSecurityService = Stub(SpringSecurityService)
        }

        obj.save(validate: false)

        assert obj.id > 0
        return obj
    }
}
