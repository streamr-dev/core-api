package com.unifina.service

import com.unifina.domain.marketplace.Product
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageVerifier

class ProductImageService {
	FileUploadProvider fileUploadProvider
	ImageVerifier imageVerifier = new ImageVerifier(1024*1024*5, 500, 400)
	IdGenerator idGenerator = new IdGenerator()

	void replaceImage(Product product, byte[] fileBytes) {
		imageVerifier.verifyImage(fileBytes)
		String newImageUrl = fileUploadProvider.uploadFile("product-images/${idGenerator.generate()}", fileBytes)
		if (product.imageUrl) {
			fileUploadProvider.deleteFile(product.imageUrl)
		}
		product.imageUrl = newImageUrl
		product.save(failOnError: true)
	}
}
