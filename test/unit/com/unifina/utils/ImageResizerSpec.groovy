package com.unifina.utils

import com.unifina.api.ApiException
import spock.lang.Specification

class ImageResizerSpec extends Specification {
	void "guessImageFormat() should throw ApiException on empty filename"() {
		def resizer = new ImageResizer()
		when:
		resizer.guessImageFormat("")
		then:
		thrown(ApiException)
	}
	void "guessImageFormat() returns file name extension"() {
		def resizer = new ImageResizer()
		when:
		def ext = resizer.guessImageFormat("filename.JPG")
		then:
		ext == "jpg"
	}
}
