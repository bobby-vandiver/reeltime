package in.reeltime.user

class UserService {

    boolean userExists(String username) {
        User.findByUsername(username) != null
    }
}
