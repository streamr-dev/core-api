package com.unifina.utils

import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class ImageVerifierSpec extends Specification {

	def imageVerifier = new ImageVerifier(16384)

	void "verifyImage() throws FileTooLargeException if size of file too large"() {
		when:
		imageVerifier.verifyImage(new byte[16385])
		then:
		def e = thrown(ImageVerifier.FileTooLargeException)
		e.message == "File size was 16385 bytes (> 16384 bytes)"
	}

	void "verifyImage() throws UnsupportedFileTypeException if file not a recognized image"() {
		when:
		imageVerifier.verifyImage(new byte[8192])
		then:
		thrown(ImageVerifier.UnsupportedFileTypeException)
	}

	void "verifyImage() does not throw if file an image of expected size"() {
		setup: "create jpg image bytes"
		def outputStream = new ByteArrayOutputStream()
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
		ImageIO.write(image, "gif", outputStream)
		byte[] imageBytes = outputStream.toByteArray()

		when:
		imageVerifier.verifyImage(imageBytes)
		then:
		noExceptionThrown()
	}
}
