package com.unifina.controller.security

import com.mashape.unirest.http.Unirest
import com.unifina.domain.security.RegistrationCode
import grails.converters.JSON
import grails.util.Environment
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import groovy.text.SimpleTemplateEngine
import org.apache.log4j.Logger

import com.unifina.exceptions.UserCreationFailedException
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite

class RegisterController {

    static defaultAction = 'index'

    def mailService
    def userService
    def springSecurityService
    def signupCodeService

    def saltSource

    def log = Logger.getLogger(RegisterController)

    // override to add GET method
    static allowedMethods = [register: ['GET', 'POST']]
	
    def index() {
        render status: 404
    }

    def register(RegisterCommand cmd) {
        def conf = SpringSecurityUtils.securityConfig
        String defaultTargetUrl = conf.successHandler.defaultTargetUrl

        def invite = SignupInvite.findByCode(cmd.invite)
        if (!invite || invite.used || !invite.sent) {
            flash.message = "Sorry, that is not a valid invitation code"
            if (invite) {
				flash.message += ". Code: $invite.code"
			}
            redirect action: 'signup'
            return
        }

        log.info('Activated invite for '+invite.username+', '+invite.code)

        if (request.method == "GET") {
            // activated the registration, but not yet submitted it
            return render(view: 'register', model: [user: [username: invite.username], invite: invite.code])
        }

        def user

        cmd.username = invite.username

        if (cmd.hasErrors()) {
            log.warn("Registration command has errors: "+userService.checkErrors(cmd.errors.getAllErrors()))
            return render(view: 'register', model: [user: cmd, invite: invite.code])
        }

        try {
            user = userService.createUser(cmd.properties)
        } catch (UserCreationFailedException e) {
            flash.message = e.getMessage()
            return render(view: 'register', model: [user: cmd, invite: invite.code])
        }

        invite.used = true
        if (!invite.save(flush: true)) {
            log.warn("Failed to save invite: "+invite.errors)
            flash.message = "Failed to save invite"
            return render(view: 'register', model: [user: user, invite: invite.code])
        }

        mailService.sendMail {
            from grailsApplication.config.unifina.email.sender
            to user.username
            subject grailsApplication.config.unifina.email.welcome.subject
            html g.render(template:"email_welcome", model: [user: user], plugin:'unifina-core')
        }

        log.info("Logging in "+user.username+" after registering")
        springSecurityService.reauthenticate(user.username)

        flash.message = "Account created!"
        redirect uri: conf.ui.register.postRegisterUrl ?: defaultTargetUrl
    }

    def signup(EmailCommand cmd) {
        if (request.method != "POST") {
            return [ user: new EmailCommand() ]
        }
        if (cmd.hasErrors()) {
            render view: 'signup', model: [ user: cmd ]
            return
        }

		if (grailsApplication.config.streamr.signup.requireCaptcha) {
			def response = Unirest.post(grailsApplication.config.recaptcha.verifyUrl)
				.field("secret", (String) grailsApplication.config.recaptchav2.secret)
				.field("response", (String) params."g-recaptcha-response")
				.asJson()
			if (response.body.jsonObject.success != true) {
				flash.error = "Confirming reCaptcha failed for some reason. Please refresh page and refill form."
				render view: 'signup', model: [user: cmd]
				return
			}
		}

		SignupInvite invite
		if (Environment.current == Environment.TEST) {
			// kludge needed for RegisterSpec."registering can now be done correctly"()
			invite = new SignupInvite(
					username: cmd.username,
					code: cmd.username.replaceAll("@", "_"),
					sent: true,
					used: false
			)
			invite.save()
		} else {
			invite = signupCodeService.create(cmd.username)
		}

		if (grailsApplication.config.streamr.signup.requireInvite) {
			mailService.sendMail {
				from grailsApplication.config.unifina.email.sender
				to invite.username
				subject grailsApplication.config.unifina.email.waitForInvite.subject
				html g.render(template: "email_wait_for_invite", model: [user: invite], plugin: 'unifina-core')
			}
			render view: 'waitForInvitation'

		} else {
			invite.sent = true
			invite.save(flush: true, failOnError:true)
			mailService.sendMail {
				from grailsApplication.config.unifina.email.sender
				to invite.username
				subject grailsApplication.config.unifina.email.registerLink.subject
				html g.render(template:"email_register_link", model:[user: invite], plugin:'unifina-core')
			}
			render view: 'registerLinkSent'

		}
		log.info("Signed up $invite.username")
    }

    @Secured(["ROLE_ADMIN"])
    def list() {
        [invites: SignupInvite.findAllByUsed(false)]
    }

    @Secured(["ROLE_ADMIN"])
    def sendInvite() {
        def invite = SignupInvite.findByCode(params.code)
        if (!invite) {
            flash.message = 'Invitation code not found'
            redirect action: 'list'
            return
        }

        invite.sent = true
        invite.save()

        mailService.sendMail {
            from grailsApplication.config.unifina.email.sender
            to invite.username
            subject grailsApplication.config.unifina.email.invite.subject
            html g.render(template:"email_invite", model:[user: invite], plugin:'unifina-core')
        }
		
        flash.message = "Invitation was sent to $invite.username"
        log.info("Invitation sent to $invite.username")

        redirect action: 'list'
    }

    def forgotPassword(EmailCommand cmd) {
        if (request.method != 'POST') {
            return
        }

        if (!cmd.validate()) {
            render view: 'forgotPassword', model: [ user: cmd ]
            return 
        }

        def user = SecUser.findWhere(username: cmd.username)
        if (!user) {
            return [emailSent: true] // don't reveal users
        }

        def registrationCode = new RegistrationCode(username: user.username)
        registrationCode.save(flush: true)

        String url = generateLink('resetPassword', [t: registrationCode.token])

        mailService.sendMail {
            from grailsApplication.config.unifina.email.sender
            to user.username
			subject grailsApplication.config.unifina.email.forgotPassword.subject
			html g.render(template:"email_forgot_password", model:[token:registrationCode.token], plugin:'unifina-core')
        }

        [emailSent: true]
    }
	
    def resetPassword(ResetPasswordCommand command) {

        String token = params.t

        def registrationCode = token ? RegistrationCode.findByToken(token) : null
		
        if (!registrationCode) {
            flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
            return
        }
		
        def user = SecUser.findByUsername(registrationCode.username)
        if (!user)
        throw new RuntimeException("User belonging to the registration code was not found: $registrationCode.username")
				
        if (!request.post) {
            log.info("Password reset code activated for user $registrationCode.username")
            return [token: token, command: new ResetPasswordCommand(), user:user]
        }

        command.username = registrationCode.username
        command.validate()

        if (command.hasErrors()) {
            return [token: token, command: command, user:user]
        }

        String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
        RegistrationCode.withTransaction { status ->
            user.password = springSecurityService.encodePassword(command.password, salt)
            user.save()
            registrationCode.delete()
        }

        springSecurityService.reauthenticate registrationCode.username

        flash.message = message(code: 'spring.security.ui.resetPassword.success')

        def conf = SpringSecurityUtils.securityConfig
        String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
        redirect uri: postResetUrl
    }

    protected String generateLink(String action, linkParams) {
        createLink(base: "$request.scheme://$request.serverName:$request.serverPort$request.contextPath",
                controller: 'register', action: action,
                params: linkParams)
    }

    protected String evaluate(s, binding) {
        new SimpleTemplateEngine().createTemplate(s).make(binding)
    }

}

class EmailCommand {
    String username
    static constraints = {
        username blank: false, email: true
    }
}

class RegisterCommand {
    String invite
    String username
    String name
    String password
    String password2
    String timezone
    String tosConfirmed
    Integer pwdStrength
	
    def userService

    static constraints = {
        importFrom SecUser

        invite blank: false

        tosConfirmed blank: false, validator: { val ->
            println('tosConfirmed '+ val)
            val == 'on'
        }

        timezone blank: false
        name blank: false
				
        password validator: {String password, RegisterCommand command ->
            return command.userService.passwordValidator(password, command)
        }
        password2 validator: {value, RegisterCommand command ->
            return command.userService.password2Validator(value, command)
        }

    }
}

class ResetPasswordCommand {
    String username
    String password
    String password2
    Integer pwdStrength

    def userService
	
    static constraints = {
        password validator: {String password, ResetPasswordCommand command ->
            return command.userService.passwordValidator(password, command)
        }
        password2 validator: {value, ResetPasswordCommand command ->
            return command.userService.password2Validator(value, command)
        }
    }
}
