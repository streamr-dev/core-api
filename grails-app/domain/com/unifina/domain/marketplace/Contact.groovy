package com.unifina.domain.marketplace

import com.unifina.utils.EmailValidator
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@GrailsCompileStatic
@Validateable
class Contact {
	// Contact's email address.
	String email
	// Contact's URL address.
	String url
	// Social media link 1
	String social1
	// Social media link 2
	String social2
	// Social media link 3
	String social3
	// Social media link 4
	String social4

	static constraints = {
		email(nullable: true, validator: EmailValidator.validateNullEmail, maxSize: 255)
		url(nullable: true, url: true, maxSize: 2048)
		social1(nullable: true, url: true, maxSize: 2048)
		social2(nullable: true, url: true, maxSize: 2048)
		social3(nullable: true, url: true, maxSize: 2048)
		social4(nullable: true, url: true, maxSize: 2048)
	}

	Map toMap() {
		return [
			email: email,
			url: url,
			social1: social1,
			social2: social2,
			social3: social3,
			social4: social4,
		]
	}
}
