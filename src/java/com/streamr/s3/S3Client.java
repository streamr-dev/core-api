package com.streamr.s3;

import java.net.URL;

interface S3Client {
	URL uploadFile(String fileName, byte[] contents);

	void deleteFile(String fileName);

	/**
	 * Makes a copy of a file in given {@code fileAmazonS3Uri} to {@code destinationKey} .
	 *
	 * @param fileAmazonS3Uri URL to Amazon S3 file
	 * @param destinationKey  Name of the copy as an S3 key.
	 * @return URL to new file
	 */
	String copyFile(String fileAmazonS3Uri, String destinationKey);
}
