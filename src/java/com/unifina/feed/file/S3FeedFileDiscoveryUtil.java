package com.unifina.feed.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.unifina.domain.data.Feed;
import com.unifina.utils.MapTraversal;

public abstract class S3FeedFileDiscoveryUtil extends AbstractFeedFileDiscoveryUtil {

	private static final Logger log = Logger.getLogger(S3FeedFileDiscoveryUtil.class);

	private AWSCredentials myCredentials;
	private AmazonS3 s3Client;
	private String bucketName;
	private String prefix;
	
	public S3FeedFileDiscoveryUtil(GrailsApplication grailsApplication,
			Feed feed, Map<String, Object> config) {
		super(grailsApplication, feed, config);
		
		myCredentials = new BasicAWSCredentials(
				MapTraversal.getString(grailsApplication.getConfig(), "unifina.feed.s3FileStorageAdapter.accessKey"),
				MapTraversal.getString(grailsApplication.getConfig(), "unifina.feed.s3FileStorageAdapter.secretKey"));
			
		bucketName = MapTraversal.getString(grailsApplication.getConfig(), "unifina.feed.s3FileStorageAdapter.bucket");
		prefix = (String) config.get("prefix");
	}

	@Override
	protected void connect() {
		s3Client = new AmazonS3Client(myCredentials);
	}

	@Override
	protected void disconnect() {
		
	}

	@Override
	protected List<String> listFiles() {
		log.info("Listing files in S3 bucket "+bucketName+" with prefix "+prefix+"...");
		List<String> result = new ArrayList<>();
		
		ObjectListing ls;
		do {
			ls = s3Client.listObjects(bucketName, prefix);
			for (S3ObjectSummary s : ls.getObjectSummaries()) {
				result.add(s.getKey());
			}
				
			ls.setMarker(ls.getNextMarker());
		}
		while (ls.isTruncated());
		
		return result;
	}
	
	/**
	 * Specify the S3FileStorageAdapter in config. It is needed to retrieve
	 * the file.
	 */
	@Override
	protected RemoteFeedFile createRemoteFeedFile(String location) {
		RemoteFeedFile file = super.createRemoteFeedFile(location);
		file.setFileStorageAdapter(S3FileStorageAdapter.class);
		return file;
	}
	
}
