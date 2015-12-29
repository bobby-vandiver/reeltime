package in.reeltime.common

import org.apache.commons.lang.builder.HashCodeBuilder

abstract class AbstractJoinDomain {

    abstract String getLeftPropertyName()

    abstract String getRightPropertyName()

    @Override
    int hashCode() {
        def builder = new HashCodeBuilder()

        def left = this."$leftPropertyName"
        if (left) {
            builder.append(left.id)
        }

        def right = this."$rightPropertyName"
        if (right) {
            builder.append(right.id)
        }

        return builder.toHashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (obj.class != this.class) {
            return false
        }

        boolean sameLeft = (obj."$leftPropertyName"?.id == this."$leftPropertyName"?.id)
        boolean sameRight = (obj."$rightPropertyName"?.id == this."$rightPropertyName"?.id)

        return sameLeft && sameRight
    }
}
