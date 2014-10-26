package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.search.PagedListCommand
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class VideoController extends AbstractController {

    def videoService

    static allowedMethods = [listVideos: 'GET']

    @Secured(["#oauth2.hasScope('videos-read')"])
    def listVideos(PagedListCommand command) {
        log.debug "Listing all videos on page [${command.page}]"
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(videoService.listVideos(command.page))
            }
        }
    }
}
