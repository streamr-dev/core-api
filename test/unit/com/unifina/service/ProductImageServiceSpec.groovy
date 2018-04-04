package com.unifina.service

import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.ImageResizer
import com.unifina.utils.testutils.FakeIdGenerator
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductImageService)
@Mock(Product)
class ProductImageServiceSpec extends Specification {

	Product product
	String filename = "picture.png"

	void setup() {
		service.imageResizer = Stub(ImageResizer)
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

	void "replaceImage() uploads image via fileUploadProvider#uploadFile"() {
		def fileUploadProvider = service.fileUploadProvider = Mock(FileUploadProvider)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		2 * fileUploadProvider.uploadFile(_, _)
	}

	void "replaceImage() does not invoke fileUploadProvider#deleteFile if Product does not have existing image"() {
		def fileUploadProvider = service.fileUploadProvider = Mock(FileUploadProvider)
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		0 * fileUploadProvider.deleteFile(_)
	}

	void "replaceImage() invokes fileUploadProvider#deleteFile if Product has existing image"() {
		def fileUploadProvider = service.fileUploadProvider = Mock(FileUploadProvider)
		def bytes = new byte[256]
		product.imageUrl = "https://www.streamr.com/files/2.png"
		product.thumbnailUrl = "https://www.streamr.com/files/t.png"

		when:
		service.replaceImage(product, bytes, filename)
		then:
		1 * fileUploadProvider.deleteFile("https://www.streamr.com/files/2.png")
		1 * fileUploadProvider.deleteFile("https://www.streamr.com/files/t.png")
	}

	void "replaceImage() updates Product.imageUrl"() {
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		Product.get("1").imageUrl == "https://www.streamr.com/files/id-0"
	}

	void "replaceImage() updates Product.thumbnailUrl"() {
		def bytes = new byte[256]

		when:
		service.replaceImage(product, bytes, filename)
		then:
		Product.get("1").thumbnailUrl == "https://www.streamr.com/files/id-0"
	}
}
