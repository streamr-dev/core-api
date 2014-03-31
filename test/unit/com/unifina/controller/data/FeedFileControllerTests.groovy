package com.unifina.controller.data

import grails.test.mixin.*
import grails.test.mixin.support.*

import java.nio.file.Files
import java.nio.file.Path

import org.junit.*


@TestFor(FeedFileController)
class FeedFileControllerTests {

	Path tempDir
	
    void setUp() {
		tempDir = Files.createTempDirectory("FeedFileControllerTests")
		grailsApplication.config.unifina.feed.datadir = tempDir.toString()
    }

    void tearDown() {
		Files.delete(tempDir)
    }

	void testGet() {
		Path feedDir = tempDir.resolve("TEST")
		Path dayDir = feedDir.resolve("20140101")
		Path file = dayDir.resolve("POST-TEST.txt")
		
		Files.createDirectories(dayDir)
		Files.createFile(file)
		
		params.feedDir = "TEST"
		params.day = "20140101"
		params.file = "POST-TEST.txt"
		
		request.method = "GET"
		controller.index()
		assert response.status == 200
		assert response.contentType == "application/octet-stream"
		
		Files.delete(file)
		Files.delete(dayDir)
		Files.delete(feedDir)
	}
	
    void testPost() {
		params.feedDir = "TEST"
		params.day = "20140101"
		params.file = "POST-TEST.txt"
		
		request.method = "POST"
		request.setContent("FOO".getBytes())
		controller.index()
		assert response.status == 200
		
		Path feedDir = tempDir.resolve("TEST")
		Path dayDir = feedDir.resolve("20140101")
		Path file = dayDir.resolve("POST-TEST.txt")
		
		String line
		file.toFile().eachLine {
			line = it
		}
		assert line == "FOO"
		
		Files.delete(file)
		Files.delete(dayDir)
		Files.delete(feedDir)
    }
}
