package in.reeltime.injection

import org.springframework.context.ApplicationContext
import in.reeltime.metadata.StreamMetadata

class ConfigInjector {

    static void injectConfigurableProperties(ConfigObject config, ApplicationContext ctx) {

        StreamMetadata.maxDuration = config.reeltime.metadata.maxDurationInSeconds as int
        enforceReadOnlyStaticField(StreamMetadata, 'maxDuration')

        ctx.videoCreationService.with {
            maxVideoStreamSizeInBytes = config.reeltime.metadata.maxVideoStreamSizeInBytes as int
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

        ctx.localFileSystemService.with {
            inputBasePath = config.reeltime.storage.input
            outputBasePath = config.reeltime.storage.output
        }

        ctx.inputStorageService.with {
            inputBase = config.reeltime.storage.input
        }

        ctx.outputStorageService.with {
            outputBase = config.reeltime.storage.output
        }

        ctx.pathGenerationService.with {
            inputBase = config.reeltime.storage.input
            outputBase = config.reeltime.storage.output
        }
    }

    private static void enforceReadOnlyStaticField(Class clazz, String property) {
        def setterName = 'set' + property.capitalize()
        clazz.metaClass.'static'."$setterName" = { obj ->
            throw new ReadOnlyPropertyException(property, clazz)
        }
    }
}
