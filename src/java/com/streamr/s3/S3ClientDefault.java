package com.streamr.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.streamr.core.service.ApiException;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.net.URL;

public class S3ClientDefault implements S3Client {
	private static final Logger log = Logger.getLogger(S3ClientDefault.class);
	private final String bucketName;
	private final AmazonS3 amazonS3;

	public S3ClientDefault(String region, String bucketName) {
		this(AmazonS3ClientBuilder.standard()
				.withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
				.withRegion(region)
				.build(), bucketName);
	}

	public S3ClientDefault(AmazonS3 amazonS3, String bucketName) {
		this.amazonS3 = amazonS3;
		this.bucketName = bucketName;
	}

	@Override
	public URL uploadFile(String fileName, byte[] contents)
			throws HostingServiceException, CommunicationErrorWithHostException {
		try {
			final ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(contents.length);
			amazonS3.putObject(new PutObjectRequest(bucketName,
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
		return amazonS3.getUrl(bucketName, fileName);
	}

	@Override
	public void deleteFile(String url) {
		try {
			AmazonS3URI s3Uri = new AmazonS3URI(url);
			amazonS3.deleteObject(new DeleteObjectRequest(s3Uri.getBucket(), s3Uri.getKey()));
		} catch (AmazonServiceException e) {
			log.error(e);
			throw new HostingServiceException();
		} catch (AmazonClientException e) {
			log.error(e);
			throw new CommunicationErrorWithHostException();
		}
	}

	@Override
	public String copyFile(String fileAmazonS3Uri, String destinationKey) {
		AmazonS3URI s3Uri = new AmazonS3URI(fileAmazonS3Uri);
		String sourceKey = s3Uri.getKey();
		CopyObjectRequest copyObjRequest = new CopyObjectRequest(this.bucketName, sourceKey, this.bucketName, destinationKey);
		try {
			amazonS3.copyObject(copyObjRequest);
		} catch (AmazonServiceException e) {
			log.error(e);
			throw new HostingServiceException();
		} catch (AmazonClientException e) {
			log.error(e);
			throw new CommunicationErrorWithHostException();
		}
		URL url = amazonS3.getUrl(this.bucketName, destinationKey);
		return url.toString();
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
