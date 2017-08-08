ReelTime: A Video Blogging Application
======================================

![](./reeltime-logo.jpg =100x100)

ReelTime is a video blogging social media application that allows users to create 2 minute videos and collect them 
in custom playlists called reels. Each reel can contain videos created by the user, videos created by other users 
or a mix of videos. 

The original idea of ReelTime belongs to [Michael Carusi](https://www.michaelcarusi.com/). 
All implementation work has been done by [Bobby Vandiver](https://github.com/bobby-vandiver).

This project is provided as-is.

Clients
-------

TODO: Include link to iOS client repo. 

Running Locally
---------------

To run locally, the `FFPROBE` and `FFMPEG` environment variables should be set with the absolute paths to the
`ffprobe` and `ffmpeg` binaries provided in the `external` directory at the root of the project.

```shell
FFPROBE="./external/ffprobe" FFMPEG="./external/ffmpeg" ./gradlew run
```

Terminology
-----------

The following defines terms in the context of the application. These are represented by Grails domain classes within
the application.

* Activity
    - An event that can occur, e.g. "Joe added a new video to his Super Cool Vlogging Reel".

* Audience
    - A group of 0 or more users following a reel.
    
* Audience Member
    - A user who is following a reel.
    
* Follower
    - A user who is following another user.
    
* Followee
    - A user who is being followed by another user.
    
* Playlist
    - An HTTP Live Streaming (HLS) playlist for a video.

* Reel
    - A playlist that contains videos grouped by theme, e.g. "My Wonderful Life Reel".

* Segment
    - An HTTP Live Streaming (HLS) video segment.

* Thumbnail
    - A small thumbnail of a video.

* Video
    - A two minute video blog entry.
    
* User
    - A user who can create videos, watch videos and/or collect videos within in a Reel.

(Brief) Technical Notes
-----------------------

ReelTime started life as a Grails 2.x application and was later upgraded to run on Grails 3.x.

ReelTime is designed to run on Amazon Web Services (AWS) and leverage the Elastic Transcoder Service for turning 
storing MP4 video files in S3 and turning them into proper HTTP Live Streaming (HLS) video stream. When running 
locally, ReelTime can leverage ffmpeg and the local file system to support the same workflow. 

All endpoints in ReelTime are secured using OAuth 2.0 via the [Grails Spring Security OAuth 2.0 Provider plugin](https://github.com/bluesliverx/grails-spring-security-oauth2-provider).
A client discovery endpoint is supported to facilitate the registration of unique clients that are scoped to the user.

The included `FunctionalTestPlugin` provides support for running the comprehensive suite of functional tests against
either a local instance of the application or a remote instance running else where. See also the [reeltime-deploy]()
project for the tool that was used to provision and deploy a scaled-down acceptance test environment in AWS.

The source code along with the automated tests should be sufficient to understand how the system is designed to work. 