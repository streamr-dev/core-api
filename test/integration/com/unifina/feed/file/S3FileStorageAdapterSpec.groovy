package com.unifina.feed.file

import grails.test.spock.IntegrationSpec

import java.nio.file.Files

class S3FileStorageAdapterSpec extends IntegrationSpec {

	def grailsApplication
	S3FileStorageAdapter s3

	def setup() {
		grailsApplication.config.unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
		grailsApplication.config.unifina.feed.s3FileStorageAdapter.secretKey = "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"
		grailsApplication.config.unifina.feed.s3FileStorageAdapter.bucket = "trading-data-us"

		s3 = new S3FileStorageAdapter(grailsApplication.config)
	}

	def makeTxtFileWithContent(String fileName, String content) {
		def file = Files.createTempFile(fileName,".txt").toFile()
		file.withWriter { it.writeLine(content) }
		return file
	}

	def "it should allow retrieving stored files"() {
		File file = makeTxtFileWithContent("S3-test", "Foo")
		s3.store(file, "test/test.txt")

		when:
		def content = s3.retrieve("test/test.txt").text

		then:
		content == "Foo\n"

		cleanup:
		file.delete()

	}

	def "it should allow deleting stored files"() {
		File file = makeTxtFileWithContent("S3-delete-test", "Bar")
		s3.store(file, "test/delete-test.txt")

		when:
		s3.delete("test/delete-test.txt")

		then:
		s3.retrieve("test/delete-test.txt") == null

		cleanup:
		file.delete()
	}


	def "it should allow overwriting stored files"() {
		File file1 = makeTxtFileWithContent("S3-test", "1")
		File file2 = makeTxtFileWithContent("S3-test", "2")

		when:
		s3.store(file1, "test/overwrite.txt")
		then:
		s3.retrieve("test/overwrite.txt").text == "1\n"

		when:
		s3.store(file2, "test/overwrite.txt")
		then:
		s3.retrieve("test/overwrite.txt").text == "2\n"

		cleanup:
		file1.delete()
		file2.delete()
	}
}
