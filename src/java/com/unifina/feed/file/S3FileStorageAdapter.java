package com.unifina.feed.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.unifina.utils.MapTraversal;

public class S3FileStorageAdapter extends FileStorageAdapter {

	private AWSCredentials myCredentials;
	private AmazonS3 s3Client;
	private String bucketName;
	
	public S3FileStorageAdapter(Map<String, Object> config) {
		super(config);
		
		myCredentials = new BasicAWSCredentials(
				MapTraversal.getString(config, "unifina.feed.s3FileStorageAdapter.accessKey"),
				MapTraversal.getString(config, "unifina.feed.s3FileStorageAdapter.secretKey"));
			
		s3Client = new AmazonS3Client(myCredentials);
		bucketName = MapTraversal.getString(config, "unifina.feed.s3FileStorageAdapter.bucket");
	}

	@Override
	protected InputStream tryRetrieve(String canonicalPath) throws IOException {
		S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, canonicalPath));
		InputStream objectData = object.getObjectContent();
		return objectData;
	}

	@Override
	protected void tryStore(File file, String canonicalPath) throws IOException {
		// Amazon S3 client retries internally, may log Exceptions to log file on each retry
		s3Client.putObject(new PutObjectRequest(bucketName, canonicalPath, file));
	}

}
