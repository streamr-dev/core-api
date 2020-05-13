package com.unifina.domain.security

import com.unifina.security.Userish
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.UsernameValidator
import grails.persistence.Entity
import groovy.transform.CompileStatic
import org.apache.commons.codec.digest.DigestUtils

@Entity
class SecUser implements Userish {

	Long id
	String username
	String password
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

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
		password blank: false
		name blank: false
		dateCreated nullable: true
		lastLogin nullable: true
		imageUrlSmall nullable: true
		imageUrlLarge nullable: true
	}

	static mapping = {
		password column: '`password`'
		permissions cascade: 'all-delete-orphan'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
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
			imageUrlSmall : imageUrlSmall,
			imageUrlLarge : imageUrlLarge,
			lastLogin: lastLogin
		]
	}

	@Override
	boolean equals(Object obj) {
		if (obj instanceof SecUser) {
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
	static SecUser loadViaJava(Long userId) {
		SecUser.load(userId)
	}

	@CompileStatic
	static SecUser getViaJava(Long userId) {
		SecUser.get(userId)
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
}
