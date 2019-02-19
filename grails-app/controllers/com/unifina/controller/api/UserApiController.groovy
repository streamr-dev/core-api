package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.UserAvatarImageService
import com.unifina.service.UserService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class UserApiController {
	static allowedMethods = [
		getUserInfo: "GET",
		delete: "DELETE",
		uploadAvatarImage: "POST",
	]

	UserService userService
	UserAvatarImageService userAvatarImageService

	@StreamrApi
	def getUserInfo() {
		render(request.apiUser?.toMap() as JSON)
	}

	@StreamrApi
	def delete(Long id) {
		SecUser user = (SecUser) request.apiUser
		userService.delete(user, id)
		render(status: 204, "")
	}

	@StreamrApi
	def uploadAvatarImage() {
		SecUser user = loggedInUser()
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

	SecUser loggedInUser() {
		request.apiUser
	}
}
