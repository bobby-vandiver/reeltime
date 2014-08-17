package in.reeltime.user

import in.reeltime.video.Video
import in.reeltime.oauth2.Client
import in.reeltime.reel.Reel

class User {

	transient springSecurityService

	String email
	String username
	String password
	boolean verified
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

    static hasMany = [videos: Video, clients: Client, reels: Reel]

	static transients = ['springSecurityService']

	static constraints = {
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
        reels.find { reel -> reel.name == reelName } != null
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
