package com.unifina.service

import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.ImageVerifier
import com.unifina.utils.testutils.FakeIdGenerator
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductImageService)
@Mock(Product)
class ProductImageServiceSpec extends Specification {

	Product product

	void setup() {
		service.imageVerifier = Stub(ImageVerifier)
		service.fileUploadProvider = Stub(FileUploadProvider) {
			uploadFile(_, _) >> new URL("https://www.streamr.com/files/id-0")
		}
		service.idGenerator = new FakeIdGenerator()

		product = new Product(
			name: "Product name",
			description: "Product description",
			category: new Category(name: "category").save(failOnError: true),
			streams: [],
			state: Product.State.NOT_DEPLOYED,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			pricePerSecond: 2
		).save(failOnError: true)
	}

	void "replaceImage() verifies image bytes via imageVerifier#verifyImage"() {
		def imageVerifier = service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes)
		then:
		1 * imageVerifier.verifyImage(bytes)
	}

	void "replaceImage() uploads image via fileUploadProvider#uploadFile"() {
		def fileUploadProvider = service.fileUploadProvider = Mock(FileUploadProvider)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes)
		then:
		1 * fileUploadProvider.uploadFile("product-images/id-0", bytes)
	}

	void "replaceImage() does not invoke fileUploadProvider#deleteFile if Product does not have existing image"() {
		def fileUploadProvider = service.fileUploadProvider = Mock(FileUploadProvider)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes)
		then:
		0 * fileUploadProvider.deleteFile(_)
	}

	void "replaceImage() invokes fileUploadProvider#deleteFile if Product has existing image"() {
		def fileUploadProvider = service.fileUploadProvider = Mock(FileUploadProvider)
		def bytes = new byte[256]
		product.imageUrl = "https://www.streamr.com/files/2.png"

		when:
		service.replaceImage(product, bytes)
		then:
		1 * fileUploadProvider.deleteFile("https://www.streamr.com/files/2.png")
	}

	void "replaceImage() updates Product.imageUrl"() {
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes)
		then:
		Product.get("1").imageUrl == "https://www.streamr.com/files/id-0"
	}
}
