package com.streamr.core.service

import com.streamr.core.domain.Category
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import com.streamr.s3.S3Client
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductImageService)
@Mock([Category, Product, User])
class ProductImageServiceSpec extends Specification {

	Product product
	String filename = "picture.png"

	void setup() {
		service.imageResizer = Stub(ImageResizer)
		service.s3Client = Stub(S3Client) {
			uploadFile(_, _) >> new URL("https://streamr.network/files/id-0")
		}

		User user = new User(
			username: "user@domain.com",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		product = new Product(
			name: "Product name",
			description: "Product description",
			category: new Category(name: "category").save(failOnError: true),
			streams: [],
			state: Product.State.NOT_DEPLOYED,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 2,
			owner: user
		).save(failOnError: true, validate: false)
	}

	void "replaceImage() throws ApiException is file extension is undefined"() {
		def bytes = new byte[256]
		service.imageResizer = Mock(ImageResizer)
		service.imageVerifier = Mock(ImageVerifier)

		when:
		service.replaceImage(product, bytes, "filename")

		then:
		thrown(ApiException)
	}

	void "replaceImage() verifies image bytes via imageVerifier#verifyImage"() {
		def imageVerifier = service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		1 * imageVerifier.verifyImage(bytes)
	}

	void "replaceImage() resizes image via imageResizer#resize"() {
		def imageResizer = service.imageResizer = Mock(ImageResizer)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		1 * imageResizer.resize(bytes, filename, ImageResizer.Size.PRODUCT_THUMB)
		1 * imageResizer.resize(bytes, filename, ImageResizer.Size.PRODUCT_HERO)
	}

	void "replaceImage() uploads image via fileUploadProvider#uploadFile"() {
		def fileUploadProvider = service.s3Client = Mock(S3Client)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		2 * fileUploadProvider.uploadFile(_, _)
	}

	void "replaceImage() does not invoke fileUploadProvider#deleteFile if Product does not have existing image"() {
		def fileUploadProvider = service.s3Client = Mock(S3Client)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		0 * fileUploadProvider.deleteFile(_)
	}

	void "replaceImage() invokes fileUploadProvider#deleteFile if Product has existing image"() {
		def fileUploadProvider = service.s3Client = Mock(S3Client)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]
		product.imageUrl = "https://streamr.network/files/2.png"
		product.thumbnailUrl = "https://streamr.network/files/t.png"

		when:
		service.replaceImage(product, bytes, filename)
		then:
		1 * fileUploadProvider.deleteFile("https://streamr.network/files/2.png")
		1 * fileUploadProvider.deleteFile("https://streamr.network/files/t.png")
	}

	void "replaceImage() updates Product.imageUrl"() {
		service.imageVerifier = Mock(ImageVerifier)
		service.imageResizer = Mock(ImageResizer)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		Product.get("1").imageUrl == "https://streamr.network/files/id-0"
	}

	void "replaceImage() updates Product.thumbnailUrl"() {
		service.imageVerifier = Mock(ImageVerifier)
		service.imageResizer = Mock(ImageResizer)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		Product.get("1").thumbnailUrl == "https://streamr.network/files/id-0"
	}
}
