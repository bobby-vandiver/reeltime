import in.reeltime.user.User

class BootStrap {

    def init = { servletContext ->

        User user = new User(
                username: 'bob',
                password: 'pass',
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false
        )
        user.save(flush: true)
        log.info("Added user [${user.username}] with id [${user.id}]")
    }

    def destroy = {
    }
}
