package com.unifina.feed.twitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.utils.Globals;

public class TwitterHistoricalFeed extends AbstractHistoricalFeed {

	private int next = -1;
	private ArrayList<Status> statuses = new ArrayList<>();
	
	TwitterFactory twitterFactory;
	boolean quit = false;
	
	public TwitterHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
		twitterFactory = new TwitterFactory();
	}

	@Override
	protected IEventRecipient getEventRecipientForMessage(Object message) {
		// What key matches this message?
		// FIXME: inefficient and does not implement multiple-keywords-to-multiple-eventrecipients
		
		Status status = (Status) message;
	    for (Object key : eventRecipientsByKey.keySet()) {
	    	if (status.getText().toLowerCase().contains(key.toString()))
	    		return eventRecipientsByKey.get(key);
	    }
	    
	    // Return null if the keyword was not found in text
	    return null;
	}
	
	@Override
	public FeedEvent getNext() throws Exception {
		IEventRecipient recipient = null;
		Status s = null;
		
		// Skip messages that return null recipient
		while(next>=0) {
			s = statuses.get(next);
			recipient = getEventRecipientForMessage(s);
			next--;
			
			if (recipient==null)
				continue;
			else {
				FeedEvent result = new FeedEvent(s, s.getCreatedAt(), recipient);
				result.feed = this;
				return result;
			}
		}
		
		return null;
	}

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate)
			throws Exception {
		// Dummy implementation
		ArrayList<Date[]> result = new ArrayList<>();
		result.add(new Date[] {beginDate, endDate});
		return result;
	}

	@Override
	public void startFeed() throws Exception {
		// Connect to Twitter and download all the tweets between beginDate and endDate.
		// Store them in memory and play them back in reverse order
		// FIXME: this is a demo implementation, improve later
		
		quit = false;
		
		// The factory instance is re-useable and thread safe.
	    Twitter twitter = twitterFactory.getInstance();

	    twitter.setOAuthConsumer("QBvmFhQVIYlUQcOLVrRlQ2HJk", "ajMyfP3Wo1CL92HYRtYUyASd8vZjWADeCUYqp2Q2DVncut6WjX");
	    twitter.setOAuthAccessToken(new AccessToken("437368408-Q9fX0Op099sc7wb1kDm7yAxn3QjqZ8yQXTG6MgC9", "viEOpPxYH0KQZ6aLe2v9LYy8SQ2ijsxE0dr9kxHj8SfzS"));
	    
	    // OR together all entered keywords
	    StringBuilder sb = new StringBuilder();
	    for (Object key : eventRecipientsByKey.keySet()) {
	    	if (sb.length()>0)
	    		sb.append(" OR ");
	    	
	    	sb.append(key.toString());
	    }
	    
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    
	    Query query = new Query(sb.toString());
	    
	    // Twitter since/until are midnights on specified day, so add +1 day to enddate to capture that day
	    Date endDate = new Date(globals.getEndDate().getTime() + 24*60*60*1000);
	    
	    query.setUntil(df.format(endDate));
	    query.setSince(df.format(globals.getStartDate()));
	    query.setCount(100);
	    query.setResultType(ResultType.recent);
	    
	    long maxId = -1;
	    
	    QueryResult result = twitter.search(query);

	    while (result!=null && result.getTweets().size()>0 && !quit) {
	    	
		    for (Status status : result.getTweets()) {
		    	maxId = status.getId();
		    	
		        if (status.getCreatedAt().before(endDate) && status.getCreatedAt().after(globals.getStartDate()))
		        	statuses.add(status);
		    }
		    
		    // Try to get the next page
	    	query.setMaxId(maxId-1);
	    	result = twitter.search(query);
	    }
		
	    next = statuses.size()-1;
	    
	}

	@Override
	public void stopFeed() throws Exception {
		quit = true;
	}

}
