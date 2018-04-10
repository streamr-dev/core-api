package com.unifina.utils

import com.unifina.provider.S3FileUploadProvider
import grails.test.spock.IntegrationSpec
import groovy.transform.CompileStatic
import spock.lang.Ignore

@Ignore
class S3FileUploadProviderIntegrationSpec extends IntegrationSpec {

	S3FileUploadProvider fileUploadProvider

	void "S3 is properly configured, and uploading and deleting is working"() {
		def fileName = "test-file-" + System.currentTimeMillis()
		def dataBytes = generateData()

		when:
		URL url = fileUploadProvider.uploadFile(fileName, dataBytes)
		then:
		url.toString() == "https://streamr-dev-public.s3.eu-west-1.amazonaws.com/${fileName}"

		when:
		fileUploadProvider.deleteFile(url.toString())
		then:
		noExceptionThrown()
	}

	@CompileStatic
	byte[] generateData() {
		byte[] dataBytes = new byte[2048]
		for (int i = 0; i < dataBytes.length; ++i) {
			dataBytes[i] = (byte) i
		}
		return dataBytes
	}
}
