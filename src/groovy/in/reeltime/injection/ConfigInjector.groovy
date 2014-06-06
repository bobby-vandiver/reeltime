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
    }

    private static void enforceReadOnlyStaticField(Class clazz, String property) {
        def setterName = 'set' + property.capitalize()
        clazz.metaClass.'static'."$setterName" = { obj ->
            throw new ReadOnlyPropertyException(property, clazz)
        }
    }
}
