package com.unifina.domain.security

import com.unifina.security.Userish
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.UsernameValidator
import groovy.transform.CompileStatic

class SecUser implements Userish {

	Long id
	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	String name
	String timezone

	Set<Key> keys
	Set<Permission> permissions

	static hasMany = [permissions: Permission, keys: Key]

	static constraints = {
		username blank: false, unique: true, validator: UsernameValidator.validate
		password blank: false
		name blank: false
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
			name           : name,
			username       : username,
			timezone       : timezone,
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
}
