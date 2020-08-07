package com.unifina.domain.security

import grails.persistence.Entity
import org.apache.commons.lang.builder.HashCodeBuilder

@Entity
class SecUserSecRole implements Serializable {

	User secUser
	Role secRole

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

	static SecUserSecRole create(User secUser, Role secRole, boolean flush = false) {
		new SecUserSecRole(secUser: secUser, secRole: secRole).save(flush: flush, insert: true, failOnError: true)
	}

	static boolean remove(User secUser, Role secRole, boolean flush = false) {
		SecUserSecRole instance = SecUserSecRole.findBySecUserAndSecRole(secUser, secRole)
		if (!instance) {
			return false
		}

		instance.delete(flush: flush)
		true
	}

	static void removeAll(User secUser) {
		executeUpdate 'DELETE FROM SecUserSecRole WHERE secUser=:user', [secUser: secUser]
	}

	static void removeAll(Role secRole) {
		executeUpdate 'DELETE FROM SecUserSecRole WHERE secRole=:secRole', [secRole: secRole]
	}

	static mapping = {
		id composite: [
			'secRole',
			'secUser',
		]
		version false
	}
}
