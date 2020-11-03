package com.unifina.controller

import com.unifina.domain.EmailValidator
import com.unifina.domain.User
import com.unifina.security.PasswordEncoder
import com.unifina.service.*
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString
import org.springframework.web.multipart.MultipartFile

class UserApiController {
	PasswordEncoder passwordEncoder
	UserService userService
	UserAvatarImageService userAvatarImageService
	BalanceService balanceService

	@StreamrApi
	def update(UpdateProfileCommand cmd) {
		User user = loggedInUser()
		// Only these user fields can be updated!
		user.name = cmd.name ?: user.name
		boolean isNewEmailValid = EmailValidator.validate(cmd.email)
		if (isNewEmailValid) {
			user.email = cmd.email
		}
		if (isNewEmailValid && EmailValidator.validate(user.username)) {
			user.username = cmd.email
		}
		user = user.save(failOnError: true)
		if (user.hasErrors()) {
			log.warn("Update failed due to validation errors: " + userService.checkErrors(user.errors.getAllErrors()))
			throw new ApiException(400, "PROFILE_UPDATE_FAILED", "Profile update failed.")
		}
		return render(user.toMap() as JSON)
	}

	@StreamrApi
	def changePassword(ChangePasswordCommand cmd) {
		if (!cmd.validate()) {
			throw new ApiException(400, "PASSWORD_CHANGE_FAILED", "Password not changed!")
		}
		User user = loggedInUser()
		user.password = passwordEncoder.encodePassword(cmd.password)
		user.save(flush: false, failOnError: true)
		log.info("User $user.username changed password!")
		render(status: 204, body: "")
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def getUserInfo() {
		render(request.apiUser.toMap() as JSON)
	}

	@StreamrApi
	def delete() {
		User user = (User) request.apiUser
		userService.delete(user)
		render(status: 204, "")
	}

	@StreamrApi
	def getCurrentUserBalance() {
		Map<String, BigInteger> balances = balanceService.getDatacoinBalances(loggedInUser())
		BigInteger sum = BigInteger.ZERO;
		for(BigInteger bal : balances.values()){
			sum = sum.add(bal)
		}
		render([sum: sum] as JSON)
	}

	@StreamrApi(expectedContentTypes = ["multipart/form-data"])
	def uploadAvatarImage() {
		User user = loggedInUser()
		MultipartFile file = getUploadedFile()
		userAvatarImageService.replaceImage(user, file.bytes, file.getOriginalFilename())
		render(user.toMap() as JSON)
	}

	MultipartFile getUploadedFile() {
		MultipartFile file = request.getFile("file")
		if (file == null) {
			throw new ApiException(400, "PARAMETER_MISSING", "Parameter 'file' missing")
		}
		return file
	}

	User loggedInUser() {
		return (User) request.apiUser
	}
}


@Validateable
@ToString
class UpdateProfileCommand {
	String name
	String email
}

@Validateable
@ToString(excludes = ["currentpassword", "password", "password2"])
class ChangePasswordCommand {

	PasswordEncoder passwordEncoder
	def userService

	String username
	String currentpassword
	String password
	String password2

	static constraints = {
		username(blank: false)
		currentpassword validator: {String pwd, ChangePasswordCommand cmd->
			User user
			try {
				user = cmd.userService.getUserFromUsernameAndPassword(cmd.username, cmd.currentpassword)
			} catch (InvalidUsernameAndPasswordException e) {
				return false
			}
			if (user == null) {
				return false
			}
			def encodedPassword = user.password
			return cmd.passwordEncoder.isPasswordValid(encodedPassword, pwd)
		}
		password validator: {String password, ChangePasswordCommand command ->
			return command.userService.passwordValidator(password, command)
		}
		password2 validator: {value, ChangePasswordCommand command ->
			return command.userService.password2Validator(value, command)
		}
	}
}
