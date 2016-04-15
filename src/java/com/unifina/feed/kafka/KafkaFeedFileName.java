package com.unifina.feed.kafka;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import com.unifina.domain.data.Stream;

public class KafkaFeedFileName {
	
	private Date beginDate;
	private String streamId;
	
	public KafkaFeedFileName(String string) {
		if (!string.startsWith("kafka."))
			throw new IllegalArgumentException("String "+string+" is not a kafka feedfile name!");
		
		SimpleDateFormat fdf = new SimpleDateFormat("yyyyMMdd");
		try {
			beginDate = fdf.parse(FilenameUtils.getName(string).substring("kafka.".length(), "kafka.".length()+8));
		} catch (Exception e) {
			throw new IllegalArgumentException("String "+string+" is not a kafka feedfile name (parsing the date failed)!");
		}
		
		try {
			streamId = string.substring("kafka.".length() + 9, string.length() - ".gz".length());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("String "+string+" is not a kafka feedfile name (parsing the streamId failed)!");
		}
		
	}
	
	public KafkaFeedFileName(Stream stream, Date beginDate) {
		this.beginDate = beginDate;
		this.streamId = stream.getId();
	}
	
	public Date getBeginDate() {
		return beginDate;
	}
	
	public String getStreamId() {
		return streamId;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat fdf = new SimpleDateFormat("yyyyMMdd");
		return "kafka."+fdf.format(getBeginDate())+"."+getStreamId()+".gz";
	}
}
