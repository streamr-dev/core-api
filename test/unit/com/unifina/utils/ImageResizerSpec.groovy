package com.unifina.utils

import com.unifina.api.ApiException
import org.apache.commons.io.IOUtils
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Paths

class ImageResizerSpec extends Specification {

	ImageResizer resizer

	def setup() {
		resizer = new ImageResizer()
	}

	private byte[] bytesFromFile(String fileName) {
		return IOUtils.toByteArray(getClass().getResource("test-images/" + fileName).toURI())
	}

	private BufferedImage imageFromBytes(byte[] bytes) {
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}

	void "guessImageFormat() should throw ApiException on empty filename"() {
		when:
		resizer.guessImageFormat("")
		then:
		thrown(ApiException)
	}

	void "guessImageFormat() returns file name extension"() {
		when:
		def ext = resizer.guessImageFormat("filename.JPG")
		then:
		ext == "jpg"
	}

	@Unroll
	void "can resize #filename to size #size"(String filename, ImageResizer.Size size) {
		setup:
		def bytes = bytesFromFile(filename)
		BufferedImage image = imageFromBytes(resizer.resize(bytes, filename, size))

		// to view images when manually testing, uncomment the line below
		// ImageIO.write(image, "jpg", File.createTempFile(filename, ".jpg"))

		expect:
		image.width == size.width()
		image.height == size.height()

		where:
		filename 								| size
		"night-stars-sky-trees-62385.jpeg"      | ImageResizer.Size.HERO
		"night-stars-sky-trees-62385.jpeg"      | ImageResizer.Size.THUMB
		"beautiful-holiday-lake-358482.jpg"		| ImageResizer.Size.HERO
		"beautiful-holiday-lake-358482.jpg"		| ImageResizer.Size.THUMB
		"vertical_panorama_by_wrbl-d4yxma1.jpg" | ImageResizer.Size.HERO
		"vertical_panorama_by_wrbl-d4yxma1.jpg" | ImageResizer.Size.THUMB
		"687px-Mona_Lisa.jpg"                   | ImageResizer.Size.HERO
		"687px-Mona_Lisa.jpg"                   | ImageResizer.Size.THUMB
	}

}
