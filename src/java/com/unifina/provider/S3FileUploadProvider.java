package com.unifina.provider;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.unifina.api.ApiException;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

public class S3FileUploadProvider implements FileUploadProvider {
	private static final Logger log = Logger.getLogger(S3FileUploadProvider.class);
	private final String bucketName;
	private final AmazonS3 s3client;

	public S3FileUploadProvider(String region, String bucketName) {
		this(AmazonS3ClientBuilder.standard()
				.withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
				.withRegion(region)
				.build(), bucketName);
	}

	public S3FileUploadProvider(AmazonS3 s3client, String bucketName) {
		this.s3client = s3client;
		this.bucketName = bucketName;
	}

	@Override
	public URL uploadFile(String fileName, byte[] contents)
			throws HostingServiceException, CommunicationErrorWithHostException {
		try {
			final ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(contents.length);
			s3client.putObject(new PutObjectRequest(bucketName,
					fileName,
					new ByteArrayInputStream(contents),
					metadata));
		} catch (AmazonServiceException e) {
			log.error(e);
			throw new HostingServiceException();
		} catch (AmazonClientException e) {
			log.error(e);
			throw new CommunicationErrorWithHostException();
		}
		return s3client.getUrl(bucketName, fileName);
	}

	@Override
	public void deleteFile(String url) {
		try {
			AmazonS3URI s3Uri = new AmazonS3URI(url);
			s3client.deleteObject(new DeleteObjectRequest(s3Uri.getBucket(), s3Uri.getKey()));
		} catch (AmazonServiceException e) {
			log.error(e);
			throw new HostingServiceException();
		} catch (AmazonClientException e) {
			log.error(e);
			throw new CommunicationErrorWithHostException();
		}
	}

	static class HostingServiceException extends ApiException {
		HostingServiceException() {
			super(500, "REJECTED_BY_HOSTING_SERVICE", "File hosting service rejected command. See server logs for details.");
		}
	}

	static class CommunicationErrorWithHostException extends ApiException {
		CommunicationErrorWithHostException() {
			super(500, "COMMUNICATION_ERROR_WITH_HOST", "Communication error with file hosting service. See server logs for details.");
		}
	}
}
