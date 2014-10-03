package in.reeltime.common

import grails.converters.JSON
import in.reeltime.user.User
import in.reeltime.reel.Reel
import in.reeltime.video.Video
import in.reeltime.activity.CreateReelActivity
import in.reeltime.activity.AddVideoToReelActivity
import in.reeltime.account.RegistrationResult

class CustomMarshallerRegistrar {

    // TODO: Make this a read only private field when PostConstruct is re-enabled
    static final Map<Class, Closure> marshallers = [

        (RegistrationResult): { result ->
            return [client_id: result.clientId, client_secret: result.clientSecret]
        },

        (User): { user ->
            return [username: user.username]
        },

        (Reel): { reel ->
            return [reelId: reel.id, name: reel.name]
        },

        (Video): { video ->
            return [videoId: video.id, title: video.title]
        },

        (CreateReelActivity): { activity ->
            return [type: 'create-reel', user: activity.user, reel: activity.reel]
        },

        (AddVideoToReelActivity): { activity ->
            return [type: 'add-video-to-reel', user: activity.user, reel: activity.reel, video: activity.video]
        }
    ]

    // TODO: Re-enable PostConstruct when GRAILS-11116 is resolved
    // @PostConstruct
    void registerMarshallers() {
        marshallers.each { clazz, marshaller ->
            JSON.registerObjectMarshaller(clazz, marshaller)
        }
    }
}
