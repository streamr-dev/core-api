package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.security.User
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageResizer
import com.unifina.utils.ImageVerifier
import grails.transaction.Transactional

@Transactional
class UserAvatarImageService {
	private final long maxSize = 1024*1024*5
	FileUploadProvider fileUploadProvider
	ImageVerifier imageVerifier = new ImageVerifier(maxSize)
	IdGenerator idGenerator = new IdGenerator()
	ImageResizer imageResizer = new ImageResizer()

	def replaceImage(User user, byte[] fileBytes, String filename) {
		imageVerifier.verifyImage(fileBytes)
		final byte[] small = imageResizer.resize(fileBytes, filename, ImageResizer.Size.AVATAR_SMALL)
		final String smallImageUrl = fileUploadProvider.uploadFile(generateFilename(filename), small)
		final byte[] large = imageResizer.resize(fileBytes, filename, ImageResizer.Size.AVATAR_LARGE)
		final String largeImageUrl = fileUploadProvider.uploadFile(generateFilename(filename), large)
		if (user.imageUrlSmall != null) {
			fileUploadProvider.deleteFile(user.imageUrlSmall)
		}
		if (user.imageUrlLarge != null) {
			fileUploadProvider.deleteFile(user.imageUrlLarge)
		}
		user.imageUrlSmall = smallImageUrl
		user.imageUrlLarge = largeImageUrl
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
