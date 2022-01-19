package com.unifina.service

import com.streamr.s3.S3Client
import com.unifina.domain.Product
import com.unifina.utils.IdGenerator

class ProductImageService {
	S3Client s3Client
	private final long maxSize = 1024 * 1024 * 5
	ImageVerifier imageVerifier = new ImageVerifier(maxSize)
	IdGenerator idGenerator = new IdGenerator()
	ImageResizer imageResizer = new ImageResizer()

	void replaceImage(Product product, byte[] fileBytes, String filename) {
		imageVerifier.verifyImage(fileBytes)
		final byte[] heroBytes = imageResizer.resize(fileBytes, filename, ImageResizer.Size.PRODUCT_HERO)
		final String newImageUrl = s3Client.uploadFile(generateFilename(filename), heroBytes)
		final byte[] thumbBytes = imageResizer.resize(fileBytes, filename, ImageResizer.Size.PRODUCT_THUMB)
		final String newThumbnailUrl = s3Client.uploadFile(generateFilename(filename), thumbBytes)
		if (product.imageUrl) {
			s3Client.deleteFile(product.imageUrl)
		}
		if (product.thumbnailUrl) {
			s3Client.deleteFile(product.thumbnailUrl)
		}
		product.imageUrl = newImageUrl
		product.thumbnailUrl = newThumbnailUrl
		product.save(failOnError: true)
	}

	private String generateFilename(final String filename) {
		if (filename.indexOf(".") == -1) {
			throw new ApiException(400, "FILE_EXT_UNDEFINED", "file extension is undefined")
		}
		final String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase()
		return "product-images/${idGenerator.generate()}${extension}"
	}
}
