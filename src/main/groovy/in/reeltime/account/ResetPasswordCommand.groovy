package in.reeltime.account

import grails.validation.Validateable
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.user.User

class ResetPasswordCommand implements Validateable {

    def authenticationService
    def userService

    String username
    String new_password

    String code
    Boolean client_is_registered

    String client_id
    String client_secret

    String client_name

    static constraints = {
        username blank: false, nullable: false
        new_password blank: false, nullable: false, minSize: User.PASSWORD_MIN_SIZE

        code blank: false, nullable: false
        client_is_registered nullable: false

        client_id nullable: true, validator: registeredClientValidator
        client_secret nullable: true, validator: clientSecretValidator

        client_name nullable: true, validator: registerNewClientValidator
    }

    private static Closure registeredClientValidator = { val, obj ->
        if(!obj.client_is_registered) {
            return true
        }
        else if(val == null) {
            return 'nullable'
        }
        else if(val == '') {
            return 'blank'
        }

        // The following validation is performed only for client_id to ensure
        // no duplicate errors are reported
        def clientId = obj.client_id
        def clientSecret = obj.client_secret

        if(!obj.authenticationService.authenticateClient(clientId, clientSecret)) {
            return 'unauthenticated'
        }

        boolean userHasClient
        try {
            def user = obj.userService.loadUser(obj.username)
            userHasClient = user.clients.find { it.clientId == clientId }  != null
        }
        catch(UserNotFoundException e) {
            userHasClient = false
        }

        if(!userHasClient) {
            return 'unauthorized'
        }
    }

    private static Closure clientSecretValidator = { val, obj ->
        if(!obj.client_is_registered) {
            return true
        }
        else if(val == null) {
            return 'nullable'
        }
        else if(val == '') {
            return 'blank'
        }
    }

    private static Closure registerNewClientValidator = { val, obj ->
        if(obj.client_is_registered) {
            return true
        }
        else if(val == null) {
            return 'nullable'
        }
        else if(val == '') {
            return 'blank'
        }
    }

    boolean isRegisteredClient() {
        client_is_registered
    }

    boolean registeredClientIsAuthentic() {
        authenticationService.authenticateClient(client_id, client_secret)
    }
}
