package in.reeltime.common

import spock.lang.Specification

class AbstractCommandSpec extends Specification {

    protected void assertCommandFieldIsValid(Class commandClass, String key, Object value, boolean valid, String code) {
        def command = commandClass.newInstance((key): value)

        assert command.validate([key]) == valid
        assert command.errors.getFieldError(key)?.code == code
    }
}
