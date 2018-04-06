package com.unifina.utils

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.unifina.provider.S3FileUploadProvider
import spock.lang.Specification

class S3FileUploadProviderSpec extends Specification {
	void "uploadFile() calls AmazonS3.putObject()"() {
		def s3Client = Mock(AmazonS3)
		def fileUploadProvider = new S3FileUploadProvider(s3Client, "bucketName")
		def bytes = new byte[16]

		when:
		fileUploadProvider.uploadFile("key/filename", bytes)
		then:
		1 * s3Client.putObject({ PutObjectRequest request ->
			assert request.bucketName == "bucketName"
			assert request.key == "key/filename"
			assert request.inputStream.bytes == bytes
			return true
		})
	}

	void "uploadFile() returns URL for uploaded File"() {
		def s3Client = Stub(AmazonS3) {
			getUrl("bucketName", "key/filename") >> new URL("https://www.streamr.com/files/file.png")
		}
		def fileUploadProvider = new S3FileUploadProvider(s3Client, "bucketName")

		when:
		URL url = fileUploadProvider.uploadFile("key/filename", new byte[16])
		then:
		url.toString() == "https://www.streamr.com/files/file.png"
	}

	void "deleteFile() calls AmazonS3.deleteObject()"() {
		def s3Client = Mock(AmazonS3)
		def fileUploadProvider = new S3FileUploadProvider(s3Client, "bucketName")

		when:
		fileUploadProvider.deleteFile("https://s3-sa-east-1.amazonaws.com/bucketName/directory/filename")
		then:
		1 * s3Client.deleteObject({ DeleteObjectRequest request ->
			assert request.bucketName == "bucketName"
			assert request.key == "directory/filename"
			return true
		})
	}
}
