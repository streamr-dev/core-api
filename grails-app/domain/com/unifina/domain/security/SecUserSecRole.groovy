package com.unifina.domain.security

import org.apache.commons.lang.builder.HashCodeBuilder

class SecUserSecRole implements Serializable {

	SecUser secUser
	SecRole secRole

	boolean equals(other) {
		if (!(other instanceof SecUserSecRole)) {
			return false
		}

		other.secUser?.id == secUser?.id &&
			other.secRole?.id == secRole?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (secUser) builder.append(secUser.id)
		if (secRole) builder.append(secRole.id)
		builder.toHashCode()
	}

	static SecUserSecRole get(long secUserId, long secRoleId) {
		find 'from SecUserSecRole where secUser.id=:secUserId and secRole.id=:secRoleId',
			[secUserId: secUserId, secRoleId: secRoleId]
	}

	static SecUserSecRole create(SecUser secUser, SecRole secRole, boolean flush = false) {
		new SecUserSecRole(secUser: secUser, secRole: secRole).save(flush: flush, insert: true, failOnError: true)
	}

	static boolean remove(SecUser secUser, SecRole secRole, boolean flush = false) {
		SecUserSecRole instance = SecUserSecRole.findBySecUserAndSecRole(secUser, secRole)
		if (!instance) {
			return false
		}

		instance.delete(flush: flush)
		true
	}

	static void removeAll(SecUser secUser) {
		executeUpdate 'DELETE FROM SecUserSecRole WHERE secUser=:secUser', [secUser: secUser]
	}

	static void removeAll(SecRole secRole) {
		executeUpdate 'DELETE FROM SecUserSecRole WHERE secRole=:secRole', [secRole: secRole]
	}

	static mapping = {
		id composite: ['secRole', 'secUser']
		version false
	}
}
