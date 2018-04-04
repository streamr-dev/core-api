package com.unifina.service

import com.unifina.domain.marketplace.Product
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageResizer

class ProductImageService {
	FileUploadProvider fileUploadProvider
	IdGenerator idGenerator = new IdGenerator()
	ImageResizer imageResizer = new ImageResizer()

	void replaceImage(Product product, byte[] fileBytes, String filename) {
		String newImageUrl;
		String newThumbnailUrl;
		for (ImageResizer.Size size : ImageResizer.Size.values()) {
			byte[] bytes = imageResizer.resize(fileBytes, filename, size)
			switch (size) {
				case ImageResizer.Size.THUMB:
					newThumbnailUrl = fileUploadProvider.uploadFile(generateFilename(filename), bytes)
					break;
				case ImageResizer.Size.HERO:
					newImageUrl = fileUploadProvider.uploadFile(generateFilename(filename), bytes)
					break;
			}
		}
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
		final String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase()
		return "product-images/${idGenerator.generate()}${extension}"
	}
}
