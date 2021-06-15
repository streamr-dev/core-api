package com.streamr.s3;

import java.net.URL;

interface S3Client {
	URL uploadFile(String fileName, byte[] contents);

	void deleteFile(String fileName);
}
