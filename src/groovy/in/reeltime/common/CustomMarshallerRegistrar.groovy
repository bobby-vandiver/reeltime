package in.reeltime.common

import grails.converters.JSON
import in.reeltime.user.User
import in.reeltime.reel.Reel
import in.reeltime.video.Video
import in.reeltime.activity.CreateReelActivity
import in.reeltime.activity.AddVideoToReelActivity
import in.reeltime.account.RegistrationResult
import javax.annotation.PostConstruct

class CustomMarshallerRegistrar {

    @PostConstruct
    void registerMarshallers() {

        JSON.registerObjectMarshaller(RegistrationResult) { result ->
            return [client_id: result.clientId, client_secret: result.clientSecret]
        }

        JSON.registerObjectMarshaller(User) { user ->
            return [username: user.username]
        }

        JSON.registerObjectMarshaller(Reel) { reel ->
            return [reelId: reel.id, name: reel.name]
        }

        JSON.registerObjectMarshaller(Video) { video ->
            return [videoId: video.id, title: video.title]
        }

        JSON.registerObjectMarshaller(CreateReelActivity) { activity ->
            return [type: 'create-reel', user: activity.user, reel: activity.reel]
        }

        JSON.registerObjectMarshaller(AddVideoToReelActivity) { activity ->
            return [type: 'add-video-to-reel', user: activity.user, reel: activity.reel, video: activity.video]
        }
    }
}
