package com.unifina.domain.security

import grails.persistence.Entity

@Entity
class RegistrationCode {

    String username
    String token = UUID.randomUUID().toString().replaceAll('-', '')
    Date dateCreated

    static constraints = {
    }

    static mapping = {
        version false
    }
}
