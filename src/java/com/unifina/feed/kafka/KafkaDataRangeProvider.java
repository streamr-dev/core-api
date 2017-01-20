package com.unifina.feed.kafka;

import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractDataRangeProvider;
import com.unifina.feed.DataRange;
import com.unifina.service.FeedFileService;
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * Created by henripihkala on 25/02/16.
 *
 * TODO: get the newest message from Kafka
 * TODO: integration test
 */
public class KafkaDataRangeProvider extends AbstractDataRangeProvider {

	FeedFileService feedFileService;

	public KafkaDataRangeProvider(GrailsApplication grailsApplication) {
		super(grailsApplication);
		feedFileService = grailsApplication.getMainContext().getBean(FeedFileService.class);
	}

	@Override
	public DataRange getDataRange(Stream stream) {
		FeedFile beginFile = feedFileService.getFirstFeedFile(stream);
		if (beginFile == null)
			return null;

		FeedFile endFile = feedFileService.getLastFeedFile(stream);
		return new DataRange(beginFile.getBeginDate(), endFile.getEndDate());
	}
}
