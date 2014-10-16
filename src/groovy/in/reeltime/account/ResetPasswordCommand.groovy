package in.reeltime.account

import grails.validation.Validateable

@Validateable
class ResetPasswordCommand {

    def authenticationService

    String username
    String new_password

    String code
    Boolean client_is_registered

    String client_id
    String client_secret

    String client_name

    static constraints = {
        username blank: false, nullable: false
        new_password blank: false, nullable: false

        code blank: false, nullable: false
        client_is_registered nullable: false

        client_id validator: registeredClientValidator
        client_secret validator: registeredClientValidator

        client_name validator: registerNewClientValidator
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
