package in.reeltime.common

import grails.converters.JSON
import in.reeltime.activity.ActivityType
import in.reeltime.activity.UserReelActivity
import in.reeltime.user.User
import in.reeltime.reel.Reel
import in.reeltime.video.Video
import in.reeltime.activity.UserReelVideoActivity
import in.reeltime.account.RegistrationResult
import in.reeltime.search.SearchResult

class CustomMarshallerRegistrar {

    private final Map<Class, Closure> marshallers = [

        (RegistrationResult): { result ->
            return [client_id: result.clientId, client_secret: result.clientSecret]
        },

        (User): { user ->
            return [username: user.username, display_name: user.displayName]
        },

        (Reel): { reel ->
            return [reelId: reel.id, name: reel.name]
        },

        (Video): { video ->
            return [videoId: video.id, title: video.title]
        },

        (UserReelActivity): { activity ->
            def type = convertActivityType(activity.type)
            return [type: type, user: activity.user, reel: activity.reel]
        },

        (UserReelVideoActivity): { activity ->
            def type = convertActivityType(activity.type)
            return [type: type, user: activity.user, reel: activity.reel, video: activity.video]
        },


        (SearchResult): { result ->
            return [results: result.results]
        }
    ]

    private final Map<ActivityType, String> activityTypes = [
            (ActivityType.CreateReel): 'create-reel',
            (ActivityType.JoinReelAudience): 'join-reel-audience',
            (ActivityType.AddVideoToReel): 'add-video-to-reel'
    ]

    private String convertActivityType(ActivityType type) {
        if(activityTypes.containsKey(type)) {
            return activityTypes[type]
        }
        throw new IllegalArgumentException("Unknown ActivityType: $type")
    }

    boolean hasMarshallerAvailable(Class objectClass) {
        return objectClass != null && getMarshaller(objectClass) != null
    }

    Closure getMarshaller(Class objectClass) {
        Closure marshallerToUse = null
        marshallers.each { clazz, marshaller ->
            if(clazz.isAssignableFrom(objectClass)) {
                marshallerToUse = marshaller
            }
        }
        return marshallerToUse
    }

    // TODO: Re-enable PostConstruct when GRAILS-11116 is resolved
    // @PostConstruct
    void registerMarshallers() {
        marshallers.each { clazz, marshaller ->
            JSON.registerObjectMarshaller(clazz, marshaller)
        }
    }
}
