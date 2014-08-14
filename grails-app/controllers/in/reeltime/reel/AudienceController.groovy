package in.reeltime.reel

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ReelNotFoundException

import static in.reeltime.common.ListMarshaller.*
import static in.reeltime.common.ContentTypes.*
import static javax.servlet.http.HttpServletResponse.*

class AudienceController extends AbstractController {

    def audienceService

    static allowedMethods = [listMembers: 'GET', addMember: 'POST', removeMember: 'DELETE']

    @Secured(["#oauth2.hasScope('audiences-read')"])
    def listMembers(Long reelId) {
        log.debug "List audience members for reel [$reelId]"
        handleSingleParamRequest(reelId, 'reel.id.required') {
            def members = audienceService.listMembers(reelId)
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshallUsersList(members)
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('audiences-write')"])
    def addMember(Long reelId) {
        log.debug "Add audience member for reel [$reelId]"
        handleSingleParamRequest(reelId, 'reel.id.required') {
            audienceService.addMember(reelId)
            render(status: SC_CREATED)
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('audiences-write')"])
    def removeMember(Long reelId) {
        log.debug "Remove audience member for reel [$reelId]"
        handleSingleParamRequest(reelId, 'reel.id.required') {
            audienceService.removeMember(reelId)
            render(status: SC_OK)
        }
    }

    def handleAuthorizationException(AuthorizationException e) {
        exceptionErrorMessageResponse(e, 'audience.unauthorized', SC_FORBIDDEN)
    }

    def handleReelNotFoundException(ReelNotFoundException e) {
        exceptionErrorMessageResponse(e, 'reel.unknown', SC_BAD_REQUEST)
    }
}
