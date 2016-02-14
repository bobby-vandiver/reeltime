package in.reeltime.test.factory

import in.reeltime.user.User

trait InjectedTestUser {

    String username
    String password

    String displayName
    String email

    private User theUser

    void setUserFactoryArgs(String username, String password, String displayName, String email) {
        this.username = username
        this.password = password
        this.displayName = displayName
        this.email = email
    }

    User getUser() {
        if(theUser == null) {
            theUser = UserFactory.createUser(username, password, displayName, email)
        }
        return theUser
    }
}