package in.reeltime.video

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.exceptions.ProbeException
import in.reeltime.exceptions.TranscoderException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.search.PagedListCommand
import org.springframework.web.multipart.MultipartRequest
import static in.reeltime.common.ContentTypes.APPLICATION_JSON
import static javax.servlet.http.HttpServletResponse.*

class VideoController extends AbstractController {

    def authenticationService

    def videoService
    def videoCreationService
    def videoRemovalService

    static allowedMethods = [listVideos: 'GET', upload: 'POST', status: 'GET', removeVideo: 'DELETE']

    @Secured(["#oauth2.hasScope('videos-read')"])
    def listVideos(PagedListCommand command) {
        log.debug "Listing all videos on page [${command.page}]"
        handleCommandRequest(command) {
            render(status: SC_OK, contentType: APPLICATION_JSON) {
                marshall(videos: videoService.listVideos(command.page))
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-write')"])
    def upload(VideoCreationCommand command) {
        log.debug("Uploading video [${command.title}]")
        bindAdditionalData(command)

        try {
            if (videoCreationService.allowCreation(command)) {
                render(status: SC_ACCEPTED, contentType: APPLICATION_JSON) {
                    marshall(videoCreationService.createVideo(command))
                }
            } else {
                commandErrorMessageResponse(command, SC_BAD_REQUEST)
            }
        }
        catch(ProbeException e) {
            exceptionErrorMessageResponse(e, 'videoCreation.probe.error', SC_SERVICE_UNAVAILABLE)
        }
        catch(TranscoderException e) {
            exceptionErrorMessageResponse(e, 'videoCreation.transcoder.error', SC_SERVICE_UNAVAILABLE)
        }
    }

    private void bindAdditionalData(VideoCreationCommand command) {
        sanitizePrivateData(command)
        command.creator = authenticationService.currentUser
        command.videoStream = getVideoStreamFromRequest()
    }

    private void sanitizePrivateData(VideoCreationCommand command) {
        command.videoStreamSizeIsValid = null
        command.durationInSeconds = null
        command.h264StreamIsPresent = null
        command.aacStreamIsPresent = null
    }

    private InputStream getVideoStreamFromRequest() {
        if(request instanceof MultipartRequest) {
            return request.getFile('video')?.inputStream
        }
        else {
            return null
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-read')"])
    def getVideo(VideoCommand command) {
        handleCommandRequest(command) {
            def video = videoService.loadVideo(command.video_id)

            boolean available = video.available
            boolean currentUserIsCreator = (video.creator == authenticationService.currentUser)

            if(!available && !currentUserIsCreator) {
                throw new VideoNotFoundException("Video is unavailable and can only be found by its creator at this time")
            }

            render(status: available ? SC_OK : SC_ACCEPTED, contentType: APPLICATION_JSON) {
                marshall(video)
            }
        }
    }

    @Secured(["#oauth2.isUser() and #oauth2.hasScope('videos-write')"])
    def removeVideo(VideoCommand command) {
        log.debug "Removing video [${command.video_id}]"
        handleCommandRequest(command) {
            videoRemovalService.removeVideoById(command.video_id)
            render(status: SC_OK)
        }
    }
}
