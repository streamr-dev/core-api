package com.unifina.domain.security

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