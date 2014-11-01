package in.reeltime.reel

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.search.PagedListCommand

import static in.reeltime.common.ContentTypes.*
import static javax.servlet.http.HttpServletResponse.*

class AudienceController extends AbstractController {

    def audienceService

    static allowedMethods = [listMembers: 'GET', addMember: 'POST', removeMember: 'DELETE']

    @Secured(["#oauth2.hasScope('audiences-read')"])
    def listMembers(ReelCommand reelCommand, PagedListCommand pagedListCommand) {
        log.debug "List audience members for reel [${reelCommand.reelId}] on page [${pagedListCommand.page}]"
        handleMultipleCommandRequest([reelCommand, pagedListCommand]) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(audienceService.listMembers(reelCommand.reelId, pagedListCommand.page))
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
}
