package com.unifina.feed.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeed;
import com.unifina.utils.Globals;

public class TwitterFeed extends AbstractFeed {

	List<Client> clients = new ArrayList<>();
	
	private static final Logger log = Logger.getLogger(TwitterFeed.class);
	
	public TwitterFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
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
	public void startFeed() throws Exception {
		ArrayList<String> keywords = new ArrayList<>();
		for (Object key : eventRecipientsByKey.keySet()) {
			keywords.add(key.toString());
		}
		
		log.info("Starting Twitter feed with following subscriptions: "+keywords);
		
	   	/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
    	final BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);

    	/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
    	Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
    	StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
    	
    	// Optional: set up some followings and track terms
    	hosebirdEndpoint.trackTerms(keywords);

    	// These secrets should be read from a config file
    	Authentication hosebirdAuth = new OAuth1("QBvmFhQVIYlUQcOLVrRlQ2HJk", "ajMyfP3Wo1CL92HYRtYUyASd8vZjWADeCUYqp2Q2DVncut6WjX", "437368408-Q9fX0Op099sc7wb1kDm7yAxn3QjqZ8yQXTG6MgC9", "viEOpPxYH0KQZ6aLe2v9LYy8SQ2ijsxE0dr9kxHj8SfzS");
    	
    	ClientBuilder builder = new ClientBuilder()
    	  .name("TwitterMessageSource")                              // optional: mainly for the logs
    	  .hosts(hosebirdHosts)
    	  .authentication(hosebirdAuth)
    	  .endpoint(hosebirdEndpoint)
    	  .processor(new StringDelimitedProcessor(msgQueue));

    	final Client hosebirdClient = builder.build();
    	clients.add(hosebirdClient);
    	
    	// Attempts to establish a connection.
    	hosebirdClient.connect();
    	
    	// Start consumer thread
    	
    	Thread consumerThread = new Thread("Twitter-Consumer-"+System.currentTimeMillis()) {
    		@Override
    		public void run() {
    			while (!hosebirdClient.isDone()) {
	    			String msg;
					try {
						msg = msgQueue.take();
						Status status = TwitterObjectFactory.createStatus(msg);
						
						IEventRecipient rcpt = getEventRecipientForMessage(status);
						if (rcpt!=null) {
							eventQueue.enqueue(new FeedEvent(status, status.getCreatedAt(), rcpt)); 
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (TwitterException e) {
						e.printStackTrace();
					}
    			}
    			
    			log.info("Twitter consumer thread is quitting: "+getName());
    		}
    	};
    	consumerThread.start();

	}

	@Override
	public void stopFeed() throws Exception {
		for (Client c : clients) {
			c.stop();
		}
	}

}
