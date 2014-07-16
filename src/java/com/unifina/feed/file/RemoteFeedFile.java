package com.unifina.feed.file;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.unifina.domain.data.Feed;

public class RemoteFeedFile {

	private Feed feed;
	private Class fileStorageAdapter;
	private Map<String, Object> map = new LinkedHashMap<String, Object>();
	
	public RemoteFeedFile(String name, Date beginDate, Date endDate, Feed feed, String location, Class fileStorageAdapter) {
		setName(name);
		setLocation(location);
		setBeginDate(beginDate);
		setEndDate(endDate);
		setFeed(feed);
		if (fileStorageAdapter!=null)
			setFileStorageAdapter(fileStorageAdapter);
	}

	public String getName() {
		return map.get("name").toString();
	}

	public void setName(String name) {
		map.put("name", name);
	}

	public Date getBeginDate() {
		return new Date((Long) map.get("beginDate"));
	}

	public void setBeginDate(Date beginDate) {
		map.put("beginDate", beginDate.getTime());
	}

	public Date getEndDate() {
		return new Date((Long) map.get("endDate"));
	}

	public void setEndDate(Date endDate) {
		map.put("endDate", endDate.getTime());
	}

	public String getLocation() {
		return map.get("location").toString();
	}

	public void setLocation(String location) {
		map.put("location",location);
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
		map.put("feedId", feed.getId());
	}
	
	public Map<String,Object> getConfig() {
		return map;
	}

	public Class getFileStorageAdapter() {
		return fileStorageAdapter;
	}

	public void setFileStorageAdapter(Class fileStorageAdapter) {
		this.fileStorageAdapter = fileStorageAdapter;
		map.put("fileStorageAdapter", fileStorageAdapter);
	}

}
