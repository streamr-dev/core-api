package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.provider.FileUploadProvider
import com.unifina.utils.ImageVerifier
import com.unifina.utils.testutils.FakeIdGenerator
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(UserAvatarImageService)
@Mock(SecUser)
class UserAvatarImageServiceSpec extends Specification {
	SecUser user
	String filename = "picture.png"
    def setup() {
		service.fileUploadProvider = Stub(FileUploadProvider) {
			uploadFile(_, _) >> new URL("https://www.streamr.com/files/id-0")
		}
		service.idGenerator = new FakeIdGenerator()

		user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
	}

	void "replaceImage() throws ApiException is file extension is undefined"() {
		def bytes = new byte[256]
		service.imageVerifier = Mock(ImageVerifier)

		when:
		service.replaceImage(user, bytes, "filename")

		then:
		thrown(ApiException)
	}

	void "replaceImage() verifies image bytes via imageVerifier#verifyImage"() {
		def bytes = new byte[256]
		service.imageVerifier = Mock(ImageVerifier)

		when:
		service.replaceImage(user, bytes, filename)
		then:
		1 * service.imageVerifier.verifyImage(bytes)
	}

	void "replaceImage() uploads image via fileUploadProvider#uploadFile"() {
		service.fileUploadProvider = Mock(FileUploadProvider)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(user, bytes, filename)
		then:
		1 * service.fileUploadProvider.uploadFile(_, _)
	}

	void "replaceImage() does not invoke fileUploadProvider#deleteFile if User does not have existing image"() {
		service.fileUploadProvider = Mock(FileUploadProvider)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(user, bytes, filename)
		then:
		0 * service.fileUploadProvider.deleteFile(_)
	}

	void "replaceImage() invokes fileUploadProvider#deleteFile if User has existing image"() {
		service.fileUploadProvider = Mock(FileUploadProvider)
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]
		user.imageUrl = "https://www.streamr.com/files/2.png"

		when:
		service.replaceImage(user, bytes, filename)

		then:
		1 * service.fileUploadProvider.deleteFile("https://www.streamr.com/files/2.png")
	}

	void "replaceImage() updates Product.imageUrl"() {
		service.imageVerifier = Mock(ImageVerifier)
		def bytes = new byte[256]

		when:
		service.replaceImage(user, bytes, filename)
		then:
		SecUser.get("1").imageUrl == "https://www.streamr.com/files/id-0"
	}

}
