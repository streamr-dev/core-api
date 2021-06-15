package com.streamr.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CopyObjectRequest
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import spock.lang.Specification

class S3ClientSpec extends Specification {
	void "uploadFile() calls AmazonS3.putObject()"() {
		def amazonS3 = Mock(AmazonS3)
		def s3Client = new S3ClientDefault(amazonS3, "bucketName")
		def bytes = new byte[16]

		when:
		s3Client.uploadFile("key/filename", bytes)
		then:
		1 * amazonS3.putObject({ PutObjectRequest request ->
			assert request.bucketName == "bucketName"
			assert request.key == "key/filename"
			assert request.inputStream.bytes == bytes
			return true
		})
	}

	void "uploadFile() returns URL for uploaded File"() {
		def amazonS3 = Stub(AmazonS3) {
			getUrl("bucketName", "key/filename") >> new URL("https://streamr.network/files/file.png")
		}
		def s3Client = new S3ClientDefault(amazonS3, "bucketName")

		when:
		URL url = s3Client.uploadFile("key/filename", new byte[16])
		then:
		url.toString() == "https://streamr.network/files/file.png"
	}

	void "deleteFile() calls AmazonS3.deleteObject()"() {
		def amazonS3 = Mock(AmazonS3)
		def s3Client = new S3ClientDefault(amazonS3, "bucketName")

		when:
		s3Client.deleteFile("https://s3-sa-east-1.amazonaws.com/bucketName/directory/filename")
		then:
		1 * amazonS3.deleteObject({ DeleteObjectRequest request ->
			assert request.bucketName == "bucketName"
			assert request.key == "directory/filename"
			return true
		})
	}

	void "copyFile() calls copyObject()"() {
		def amazonS3 = Mock(AmazonS3)
		def s3Client = new S3ClientDefault(amazonS3, "streamr-public")

		when:
		s3Client.copyFile("https://streamr-public.s3.amazonaws.com/avatar-images/QVQrZgOnQCmFVNpjlnUvQw9IcHQu-oQie_tRm3iH-4vg.png", "avatar-images/random-new-file-name-jkashi3wra.png")
		then:
		1 * amazonS3.getUrl("streamr-public", "avatar-images/random-new-file-name-jkashi3wra.png") >> new URL("https://streamr-public.s3.amazonaws.com/avatar-images/random-new-file-name-jkashi3wra.png")
		1 * amazonS3.copyObject({ CopyObjectRequest request ->
			assert request.sourceBucketName == "streamr-public"
			assert request.sourceKey == "avatar-images/QVQrZgOnQCmFVNpjlnUvQw9IcHQu-oQie_tRm3iH-4vg.png"
			assert request.destinationBucketName == "streamr-public"
			assert request.destinationKey == "avatar-images/random-new-file-name-jkashi3wra.png"
			return true
		})
	}
}
