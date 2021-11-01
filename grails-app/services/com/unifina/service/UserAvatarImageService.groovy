package com.unifina.service

import com.streamr.s3.S3Client
import com.unifina.domain.User
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageResizer
import com.unifina.utils.ImageVerifier
import grails.transaction.NotTransactional
import grails.transaction.Transactional

import java.nio.file.Paths

@Transactional
class UserAvatarImageService {
	private final long maxSize = 1024 * 1024 * 5
	S3Client s3Client
	ImageVerifier imageVerifier = new ImageVerifier(maxSize)
	IdGenerator idGenerator = new IdGenerator()
	ImageResizer imageResizer = new ImageResizer()

	def replaceImage(User user, byte[] fileBytes, String filename) {
		imageVerifier.verifyImage(fileBytes)
		final byte[] small = imageResizer.resize(fileBytes, filename, ImageResizer.Size.AVATAR_SMALL)
		final String smallImageUrl = s3Client.uploadFile(generateFilename(filename), small)
		final byte[] large = imageResizer.resize(fileBytes, filename, ImageResizer.Size.AVATAR_LARGE)
		final String largeImageUrl = s3Client.uploadFile(generateFilename(filename), large)
		if (user.imageUrlSmall != null) {
			s3Client.deleteFile(user.imageUrlSmall)
		}
		if (user.imageUrlLarge != null) {
			s3Client.deleteFile(user.imageUrlLarge)
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

	@NotTransactional
	String copyImage(String imageUrl) {
		String filename = Paths.get(imageUrl).getFileName().toString()
		return s3Client.copyFile(imageUrl, generateFilename(filename))
	}
}
