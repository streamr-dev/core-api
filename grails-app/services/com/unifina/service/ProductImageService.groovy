package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.marketplace.Product
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageResizer
import com.unifina.utils.ImageVerifier

class ProductImageService {
	FileUploadProvider fileUploadProvider
	private final long maxSize = 1024*1024*5
	ImageVerifier imageVerifier = new ImageVerifier(maxSize)
	IdGenerator idGenerator = new IdGenerator()
	ImageResizer imageResizer = new ImageResizer()

	void replaceImage(Product product, byte[] fileBytes, String filename) {
		imageVerifier.verifyImage(fileBytes)
		final byte[] heroBytes = imageResizer.resize(fileBytes, filename, ImageResizer.Size.HERO)
		final String newImageUrl = fileUploadProvider.uploadFile(generateFilename(filename), heroBytes)
		final byte[] thumbBytes = imageResizer.resize(fileBytes, filename, ImageResizer.Size.THUMB)
		final String newThumbnailUrl = fileUploadProvider.uploadFile(generateFilename(filename), thumbBytes)
		if (product.imageUrl) {
			fileUploadProvider.deleteFile(product.imageUrl)
		}
		if (product.thumbnailUrl) {
			fileUploadProvider.deleteFile(product.thumbnailUrl)
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
