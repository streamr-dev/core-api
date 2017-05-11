package com.unifina.domain.security

class Account {

	SecUser user
	String name
	Type type
	String json

	static constraints = {
	}

	enum Type {
		ETHEREUM("ETHEREUM")
		String id

		Type(String id) {
			this.id = id
		}

		static fromString(String operationId) {
			return Type.enumConstants.find { it.id == operationId }
		}
	}
}

