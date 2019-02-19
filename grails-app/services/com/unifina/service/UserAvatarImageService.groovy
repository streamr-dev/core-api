package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageVerifier
import grails.transaction.Transactional

@Transactional
class UserAvatarImageService {
	private final long maxSize = 1024*1024*5
	FileUploadProvider fileUploadProvider
	ImageVerifier imageVerifier = new ImageVerifier(maxSize)
	IdGenerator idGenerator = new IdGenerator()

	def replaceImage(SecUser user, byte[] fileBytes, String filename) {
		imageVerifier.verifyImage(fileBytes)
		final String newAvatarImageUrl = fileUploadProvider.uploadFile(generateFilename(filename), fileBytes)
		if (user.imageUrl != null) {
			fileUploadProvider.deleteFile(user.imageUrl)
		}
		user.imageUrl = newAvatarImageUrl
		user.save(failOnError: true)
    }

	private String generateFilename(final String filename) {
		if (filename.indexOf(".") == -1) {
			throw new ApiException(400, "FILE_EXT_UNDEFINED", "file extension is undefined")
		}
		final String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase()
		return "avatar-images/${idGenerator.generate()}${extension}"
	}
}
