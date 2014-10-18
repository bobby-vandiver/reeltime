package in.reeltime.account

import grails.validation.Validateable
import in.reeltime.user.User

@Validateable
class ResetPasswordCommand {

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
        client_secret nullable: true, validator: registeredClientValidator

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

        def clientId = obj.client_id
        def clientSecret = obj.client_secret

        if(!obj.authenticationService.authenticateClient(clientId, clientSecret)) {
            return 'unauthenticated'
        }

        def user = obj.userService.loadUser(obj.username)
        def userHasClient = user.clients.find { it.clientId == clientId }  != null

        if(!userHasClient) {
            return 'unauthorized'
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
