package com.unifina.utils

class EmailValidator {
	static validate = { String email ->
		if (email == null) {
			return false;
		}
		return email.matches(".+@.+\\..+");
	}
}
