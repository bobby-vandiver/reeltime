package in.reeltime.user

import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.video.Video
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel

class User {

    static final DISPLAY_NAME_REGEX = /^\w{1}[\w ]{0,18}?\w{1}$/

	transient springSecurityService

    String displayName
	String email
	String username
	String password
	boolean verified
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

    static searchable = true

    static hasMany = [videos: Video, clients: Client, reels: Reel]

	static transients = ['springSecurityService']

	static constraints = {
        displayName blank: false, nullable: false, matches: DISPLAY_NAME_REGEX
        email blank: false, nullable: false, email: true
		username blank: false, nullable: false, matches: /^\w{2,15}$/, unique: true
		password blank: false, nullable: false
        clients nullable: false, minSize: 1
        reels nullable: false, minSize: 1, validator: reelsValidator
	}

	static mapping = {
		password column: '`password`'
	}

    static Closure reelsValidator = { val, obj ->
        return obj.hasReel(Reel.UNCATEGORIZED_REEL_NAME)
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
        reels.find { reel -> reel.name == reelName }
    }

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
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
		password = springSecurityService.encodePassword(password)
	}
}
