package com.unifina.controller.data

import grails.test.mixin.TestFor
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

@TestFor(LocalFeedFileController)
class LocalFeedFileControllerSpec extends Specification {

	Path tempDir
	
    void setup() {
		tempDir = Files.createTempDirectory("FeedFileControllerTests")
		grailsApplication.config.unifina.feed.datadir = tempDir.toString()
    }

    void cleanup() {
		Files.delete(tempDir)
    }

	void "get"() {
		setup:
		Path feedDir = tempDir.resolve("TEST")
		Path dayDir = feedDir.resolve("20140101")
		Path file = dayDir.resolve("POST-TEST.txt")
		
		Files.createDirectories(dayDir)
		Files.createFile(file)

		when:
		params.feedDir = "TEST"
		params.day = "20140101"
		params.file = "POST-TEST.txt"

		request.method = "GET"
		controller.index()

		then:
		response.status == 200
		response.contentType == "application/octet-stream"

		cleanup:
		Files.delete(file)
		Files.delete(dayDir)
		Files.delete(feedDir)
	}
	
    void "post"() {
		when:
		params.feedDir = "TEST"
		params.day = "20140101"
		params.file = "POST-TEST.txt"

		request.method = "POST"
		request.setContent("FOO".getBytes())
		controller.index()

		then:
		response.status == 200

		when:
		Path feedDir = tempDir.resolve("TEST")
		Path dayDir = feedDir.resolve("20140101")
		Path file = dayDir.resolve("POST-TEST.txt")
		
		String line
		file.toFile().eachLine {
			line = it
		}
		then:
		line == "FOO"

		cleanup:
		Files.delete(file)
		Files.delete(dayDir)
		Files.delete(feedDir)
    }
}
