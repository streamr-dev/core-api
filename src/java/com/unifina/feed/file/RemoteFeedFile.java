package com.unifina.feed.file;

import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.unifina.domain.data.Feed;

public class RemoteFeedFile {

	private String name;
	private Date beginDate;
	private Date endDate;
	private URL url;
	private Feed feed;
	
	public RemoteFeedFile(String name, Date beginDate, Date endDate, Feed feed, URL url) {
		this.name = name;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.feed = feed;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	
	public Map<String,Object> getConfig() {
		Map<String,Object> map = new LinkedHashMap<>();
		map.put("name", getName());
		map.put("url", getUrl().toString());
		map.put("beginDate", getBeginDate().getTime());
		map.put("endDate", getEndDate().getTime());
		map.put("feedId", getFeed().getId());
		return map;
	}

}
