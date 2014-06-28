package in.reeltime.user

import in.reeltime.oauth2.Client

class User {

	transient springSecurityService

	String email
	String username
	String password
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

    static hasMany = [clients: Client]

	static transients = ['springSecurityService']

	static constraints = {
		email blank: false, nullable: false, email: true
		username blank: false, nullable: false, matches: /^\w{2,15}$/, unique: true
		password blank: false, nullable: false
        clients nullable: false, size: 1..1
	}

	static mapping = {
		password column: '`password`'
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
