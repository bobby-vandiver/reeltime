package in.reeltime.user

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.oauth2.Client
import in.reeltime.reel.AudienceMember
import in.reeltime.reel.Reel
import in.reeltime.reel.UserReel
import in.reeltime.video.Video
import in.reeltime.video.VideoCreator

@EqualsAndHashCode(includes = ['username'])
@ToString(includes = ['username', 'displayName'], includeNames = true, includePackage=false)
class User implements Serializable {

    private static final long serialVersionUID = 1

    static final USERNAME_REGEX = /^\w{2,15}$/
    static final DISPLAY_NAME_REGEX = /^\w{1}[\w ]{0,18}?\w{1}$/
    static final PASSWORD_MIN_SIZE = 6

	transient springSecurityService

    String username
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    String displayName
	String email
	boolean verified

    static hasMany = [clients: Client]

	static transients = [
            'springSecurityService',
            'numberOfFollowees',
            'numberOfFollowers',
            'numberOfReels',
            'numberOfAudienceMemberships',
            'currentUserIsFollowing',
            'reels',
            'videos'
    ]

    static constraints = {
        displayName blank: false, nullable: false, matches: DISPLAY_NAME_REGEX
        email blank: false, nullable: false, email: true
        username blank: false, nullable: false, matches: USERNAME_REGEX, unique: true
        password blank: false, nullable: false
        clients nullable: false
    }

    static List<User> findAllByIdInListInAlphabeticalOrderByPage(List<Long> userIds, int page, int maxUsersPerPage) {
        int offset = (page - 1) * maxUsersPerPage
        return userIds.empty ? [] : User.findAllByIdInList(userIds, [max: maxUsersPerPage, offset: offset, sort: 'username'])
    }

    static List<Long> findAllClientIdsByUser(User user) {
        User.withCriteria {
            idEq(user.id)
            clients {
                projections {
                    property('id')
                }
            }
        } as List<Long>
    }

    User(String username, String password) {
        this()
        this.username = username
        this.password = password
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this)*.role
    }

    Collection<Reel> getReels() {
        UserReel.findAllByOwner(this)*.reel
    }

    Collection<Video> getVideos() {
        VideoCreator.findAllByCreator(this)*.video
    }

    boolean hasReel(String reelName) {
        return findReelByName(reelName) != null
    }

    Reel getReel(String reelName) {
        def reel = findReelByName(reelName)
        if(!reel) {
            throw new ReelNotFoundException("User [$username] does not have reel named [$reelName]")
        }
        return reel
    }

    private Reel findReelByName(String reelName) {
        if(User.exists(id)) {
            return UserReel.findAllByOwner(this)*.reel.find { it.name == reelName }
        }
        else {
            return reels.find { reel -> reel.name == reelName }
        }
    }

    int getNumberOfFollowees() {
        UserFollowing.countByFollower(this)
    }

    int getNumberOfFollowers() {
        UserFollowing.countByFollowee(this)
    }

    int getNumberOfReels() {
        UserReel.countByOwner(this)
    }

    int getNumberOfAudienceMemberships() {
        AudienceMember.countByMember(this)
    }

    boolean getCurrentUserIsFollowing() {
        def currentUser = springSecurityService.currentUser as User
        UserFollowing.findByFollowerAndFollowee(currentUser, this) != null
    }

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
	}
}
