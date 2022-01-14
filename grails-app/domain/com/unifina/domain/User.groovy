package com.unifina.domain

import grails.persistence.Entity

@Entity
class User {
	Long id
	String username
	String email
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	SignupMethod signupMethod = SignupMethod.UNKNOWN
	String name
	Set<Permission> permissions
	// dateCreated is the date when account is created.
	Date dateCreated
	// lastLogin is the date when last successful login was made.
	Date lastLogin = new Date()
	// Users avatar images.
	String imageUrlSmall
	String imageUrlLarge

	static hasMany = [permissions: Permission]

	static constraints = {
		username blank: false, unique: true, validator: UsernameValidator.validate
		email nullable: true, validator: EmailValidator.validateNullEmail
		name blank: false
		dateCreated nullable: true
		lastLogin nullable: true
		imageUrlSmall nullable: true
		imageUrlLarge nullable: true
		signupMethod enumType: "string", nullable: false
	}

	static mapping = {
		permissions cascade: 'all-delete-orphan'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { UserRole it -> it.role } as Set
	}

	boolean isDevOps() {
		"ROLE_DEV_OPS" in authorities*.authority
	}

	boolean isAdmin() {
		"ROLE_ADMIN" in authorities*.authority
	}

	Map toMap() {
		return [
			name: name,
			username: username,
			email: email,
			imageUrlSmall: imageUrlSmall,
			imageUrlLarge: imageUrlLarge,
			lastLogin: lastLogin
		]
	}

	@Override
	boolean equals(Object obj) {
		if (obj instanceof User) {
			if (obj.id == null || this.id == null) {
				return this.is(obj)
			} else {
				return obj.id == this.id
			}
		} else if (obj instanceof Long) {
			return obj == this.id
		} else {
			return false
		}
	}

	@Override
	int hashCode() {
		return this.id?.hashCode() ?: super.hashCode()
	}

	String getPublisherId() {
		return username
	}

	String getEmail() {
		if (EmailValidator.validate(email)) {
			return email
		} else if (EmailValidator.validate(username)) {
			return username
		}
		return null
	}
}
