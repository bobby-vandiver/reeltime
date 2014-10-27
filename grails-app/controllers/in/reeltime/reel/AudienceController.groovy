package in.reeltime.reel

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ReelNotFoundException

import static in.reeltime.common.ContentTypes.*
import static javax.servlet.http.HttpServletResponse.*

class AudienceController extends AbstractController {

    def audienceService

    static allowedMethods = [listMembers: 'GET', addMember: 'POST', removeMember: 'DELETE']

    @Secured(["#oauth2.hasScope('audiences-read')"])
    def listMembers(ReelCommand command) {
        log.debug "List audience members for reel [${command.reelId}]"
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(audienceService.listMembers(command.reelId))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('audiences-write')"])
    def addMember(ReelCommand command) {
        log.debug "Add audience member for reel [${command.reelId}]"
        handleCommandRequest(command) {
            audienceService.addCurrentUserToAudience(command.reelId)
            render(status: SC_CREATED)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('audiences-write')"])
    def removeMember(ReelCommand command) {
        log.debug "Remove audience member for reel [${command.reelId}]"
        handleCommandRequest(command) {
            audienceService.removeCurrentUserFromAudience(command.reelId)
            render(status: SC_OK)
        }
    }

    def handleAuthorizationException(AuthorizationException e) {
        exceptionErrorMessageResponse(e, 'audience.unauthorized', SC_FORBIDDEN)
    }

    def handleReelNotFoundException(ReelNotFoundException e) {
        exceptionErrorMessageResponse(e, 'reel.unknown', SC_NOT_FOUND)
    }
}
