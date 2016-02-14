package in.reeltime.injection

import grails.config.Config
import in.reeltime.maintenance.ResourceRemovalJob
import in.reeltime.video.VideoCreationCommand
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.encoding.PasswordEncoder

class ConfigInjector {

    static void injectConfigurableProperties(Config config, ApplicationContext ctx) {

        VideoCreationCommand.maxDuration = config.reeltime.metadata.maxDurationInSeconds as int
//        enforceReadOnlyStaticField(VideoCreationCommand, 'maxDuration')

        ResourceRemovalJob.numberToRemovePerExecution = config.reeltime.maintenance.numberOfResourcesToRemovePerExecution as int
//        enforceReadOnlyStaticField(ResourceRemovalJob, 'numberToRemovePerExecution')

        ctx.videoCreationService.with {
            maxVideoStreamSizeInBytes = config.reeltime.metadata.maxVideoStreamSizeInBytes as int
            maxThumbnailStreamSizeInBytes = config.reeltime.metadata.maxThumbnailStreamSizeInBytes as int
        }

        ctx.ffprobeService.with {
            ffprobe = config.reeltime.metadata.ffprobe
        }

        ctx.ffmpegTranscoderService.with {
            ffmpeg = config.reeltime.transcoder.ffmpeg.path
            segmentDuration = config.reeltime.transcoder.output.segmentDuration
            segmentFormat = config.reeltime.transcoder.ffmpeg.segmentFormat
        }

        ctx.elasticTranscoderService.with {
            presetIds = config.reeltime.transcoder.output.presets
            pipelineName = config.reeltime.transcoder.pipeline
            inputSettings = config.reeltime.transcoder.input
            segmentDuration = config.reeltime.transcoder.output.segmentDuration
            playlistFormat = config.reeltime.transcoder.output.format
        }

        ctx.playlistParserService.with {
            maxRetries = config.reeltime.playlistParser.maxRetries
            intervalInMillis = config.reeltime.playlistParser.intervalInMillis
        }

        ctx.pathGenerationService.with {
            maxRetries = config.reeltime.storage.pathGenerationMaxRetries
        }

        ctx.localFileSystemService.with {
            videoBasePath = config.reeltime.storage.videos
            playlistBasePath = config.reeltime.storage.playlists
        }

        ctx.videoStorageService.with {
            videoBase = config.reeltime.storage.videos
        }

        ctx.playlistAndSegmentStorageService.with {
            playlistBase = config.reeltime.storage.playlists
        }

        ctx.thumbnailStorageService.with {
            thumbnailBase = config.reeltime.storage.thumbnails
        }

        ctx.accountCodeGenerationService.with {
            costFactor = config.reeltime.accountManagement.bcryptCostFactor
        }

        ctx.accountConfirmationService.with {
            fromAddress = config.reeltime.accountManagement.fromAddress
            confirmationCodeValidityLengthInDays = config.reeltime.accountManagement.confirmationCodeValidityLengthInDays
        }

        ctx.resetPasswordService.with {
            fromAddress = config.reeltime.accountManagement.fromAddress
            resetPasswordCodeValidityLengthInMins = config.reeltime.accountManagement.resetPasswordCodeValidityLengthInMins
        }

        ctx.activityService.with {
            maxActivitiesPerPage = config.reeltime.activity.maxActivitiesPerPage
        }

        ctx.clientService.with {
            maxClientsPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.userService.with {
            maxUsersPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.userFollowingService.with {
            maxUsersPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.videoService.with {
            maxVideosPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.reelService.with {
            maxReelsPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.reelVideoManagementService.with {
            maxVideosPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.audienceService.with {
            maxMembersPerPage = config.reeltime.browse.maxResultsPerPage
        }

        ctx.authenticationService.with {
            passwordEncoder = ctx.getBean('passwordEncoder') as PasswordEncoder
        }
    }

    private static void enforceReadOnlyStaticField(Class clazz, String property) {
        def setterName = 'set' + property.capitalize()
        clazz.metaClass.'static'."$setterName" = { obj ->
            throw new ReadOnlyPropertyException(property, clazz)
        }
    }
}
