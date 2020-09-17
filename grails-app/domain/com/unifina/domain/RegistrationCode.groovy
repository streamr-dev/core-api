package com.unifina.domain


import grails.persistence.Entity

@Entity
class RegistrationCode {
    String email
    String token = UUID.randomUUID().toString().replaceAll('-', '')
    Date dateCreated

    static constraints = {
		email blank: false, validator: EmailValidator.validate
    }

    static mapping = {
        version false
    }
}
