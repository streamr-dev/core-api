package com.unifina.domain

import grails.persistence.Entity
import groovy.transform.CompileStatic
import org.apache.commons.codec.digest.DigestUtils

@Entity
class User implements Userish {

	Long id
	String username
	String email
	String password
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	SignupMethod signupMethod = SignupMethod.UNKNOWN

	String name

	Set<Key> keys
	Set<Permission> permissions

	// dateCreated is the date when account is created.
	Date dateCreated
	// lastLogin is the date when last successful login was made.
	Date lastLogin = new Date()
	// Users avatar images.
	String imageUrlSmall
	String imageUrlLarge

	static hasMany = [permissions: Permission, keys: Key]

	static constraints = {
		username blank: false, unique: true, validator: UsernameValidator.validate
		email nullable: true, validator: EmailValidator.validateNullEmail
		password blank: false
		name blank: false
		dateCreated nullable: true
		lastLogin nullable: true
		imageUrlSmall nullable: true
		imageUrlLarge nullable: true
		signupMethod nullable: false
	}

	static mapping = {
		password column: '`password`'
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
			name : name,
			username : username,
			email : email,
			imageUrlSmall : imageUrlSmall,
			imageUrlLarge : imageUrlLarge,
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

	@Override
	Userish resolveToUserish() {
		return this
	}

	@CompileStatic
	static User loadViaJava(Long userId) {
		User.load(userId)
	}

	@CompileStatic
	static User getViaJava(Long userId) {
		User.get(userId)
	}

	//TODO: Once all users are defined with their ethereum public key we can remove this
	boolean isEthereumUser() {
		return EthereumAddressValidator.validate(username)
	}

	String getPublisherId() {
		if (isEthereumUser()) {
			return username
		}
		// 'username' is the email address of the user. For privacy concerns, the publisher id is the hash of the email address.
		return DigestUtils.sha256Hex(username)
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
