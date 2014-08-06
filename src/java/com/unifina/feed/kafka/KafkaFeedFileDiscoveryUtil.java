package com.unifina.feed.kafka;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.data.Feed;
import com.unifina.feed.file.S3FeedFileDiscoveryUtil;

public class KafkaFeedFileDiscoveryUtil extends S3FeedFileDiscoveryUtil {

	public KafkaFeedFileDiscoveryUtil(GrailsApplication grailsApplication,
			Feed feed, Map<String, Object> config) {
		super(grailsApplication, feed, config);
	}

	@Override
	protected Date getBeginDate(String location) {
		SimpleDateFormat fdf = new SimpleDateFormat("yyyyMMdd");
		try {
			return fdf.parse(FilenameUtils.getName(location).substring("kafka.".length(), "kafka.".length()+8));
		} catch (ParseException e) {
			throw new RuntimeException("Failed to parse date from Kafka recording at location: "+location);
		}
	}

	@Override
	protected Date getEndDate(String location) {
		Date beginDate = getBeginDate(location);
		// Set the end date to second precision to avoid loss of precision with MySQL
		return new Date(beginDate.getTime() + 24*60*60*1000 - 1000);
	}

}
