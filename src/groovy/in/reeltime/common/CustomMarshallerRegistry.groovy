package in.reeltime.common

import grails.converters.JSON
import in.reeltime.account.RegistrationResult
import in.reeltime.activity.ActivityType
import in.reeltime.activity.UserReelActivity
import in.reeltime.activity.UserReelVideoActivity
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video

class CustomMarshallerRegistry {

    private final Map<Class, Closure> marshallers = [

        (RegistrationResult): { RegistrationResult result ->
            return [client_id: result.clientId, client_secret: result.clientSecret]
        },

        (Client): { Client client ->
            return [client_id: client.clientId, client_name: client.clientName]
        },

        (User): { User user ->
            return [
                    username: user.username,
                    display_name: user.displayName,
                    follower_count: user.numberOfFollowers,
                    followee_count: user.numberOfFollowees,
                    reel_count: user.numberOfReels,
                    audience_membership_count: user.numberOfAudienceMemberships,
                    current_user_is_following: user.currentUserIsFollowing
            ]
        },

        (Reel): { Reel reel ->
            return [
                    reel_id: reel.id,
                    name: reel.name,
                    audience_size: reel.numberOfAudienceMembers,
                    video_count: reel.numberOfVideos,
                    owner: reel.owner,
                    current_user_is_an_audience_member: reel.currentUserIsAnAudienceMember
            ]
        },

        (Video): { Video video ->
            return [video_id: video.id, title: video.title]
        },

        (UserReelActivity): { UserReelActivity activity ->
            def type = convertActivityType(activity.type)
            return [type: type, user: activity.user, reel: activity.reel]
        },

        (UserReelVideoActivity): { UserReelVideoActivity activity ->
            def type = convertActivityType(activity.type)
            return [type: type, user: activity.user, reel: activity.reel, video: activity.video]
        }
    ]

    private final Map<ActivityType, String> activityTypes = [
            (ActivityType.CreateReel): 'create-reel',
            (ActivityType.JoinReelAudience): 'join-reel-audience',
            (ActivityType.AddVideoToReel): 'add-video-to-reel'
    ]

    private String convertActivityType(Integer activityType) {
        def type = ActivityType.byValue(activityType)

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
