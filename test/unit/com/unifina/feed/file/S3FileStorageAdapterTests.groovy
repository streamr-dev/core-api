package com.unifina.feed.file

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import java.nio.file.Files

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class S3FileStorageAdapterTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testStoreAndRetrieve() {
		File file = Files.createTempFile("S3-test",".txt").toFile()
		
		try {
			file.withWriter {out->
				out.writeLine("Foo")
			}
			
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.secretKey = "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"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.bucket = "trading-data-us"
			S3FileStorageAdapter s3 = new S3FileStorageAdapter(grailsApplication.config)
			
			s3.store(file, "test/test.txt")
			
			s3.retrieve("test/test.txt").eachLine {
				assert it == "Foo"
			}
		} catch (Exception e) {
			throw e
		} finally {
			file.delete()
		}
    }
	
	void testStoreAndDelete() {
		File file = Files.createTempFile("S3-delete-test",".txt").toFile()
		
		try {
			file.withWriter {out->
				out.writeLine("Bar")
			}
			
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.secretKey = "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"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.bucket = "trading-data-us"
			S3FileStorageAdapter s3 = new S3FileStorageAdapter(grailsApplication.config)
			
			s3.store(file, "test/delete-test.txt")
			s3.delete("test/delete-test.txt")
		} catch (Exception e) {
			throw e
		} finally {
			file.delete()
		}
	}
	
	void testOverwrite() {
		File file = Files.createTempFile("S3-test",".txt").toFile()
		
		// Write first
		try {
			file.withWriter {out->
				out.writeLine("1")
			}
			
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.secretKey = "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"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.bucket = "trading-data-us"
			S3FileStorageAdapter s3 = new S3FileStorageAdapter(grailsApplication.config)
			
			s3.store(file, "test/overwrite.txt")
			
			int linecount = 0
			s3.retrieve("test/overwrite.txt").eachLine {
				assert it == "1"
				linecount++
			}
			assert linecount == 1
		} catch (Exception e) {
			throw e
		} finally {
			file.delete()
		}
		
		file = Files.createTempFile("S3-test",".txt").toFile()
		
		// Write second
		try {
			file.withWriter {out->
				out.writeLine("2")
			}
			
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.secretKey = "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"
			grailsApplication.config.unifina.feed.s3FileStorageAdapter.bucket = "trading-data-us"
			S3FileStorageAdapter s3 = new S3FileStorageAdapter(grailsApplication.config)
			
			s3.store(file, "test/overwrite.txt")
			
			int linecount = 0
			s3.retrieve("test/overwrite.txt").eachLine {
				assert it == "2"
				linecount++
			}
			assert linecount == 1
		} catch (Exception e) {
			throw e
		} finally {
			file.delete()
		}
	}

}
