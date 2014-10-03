package com.unifina.signalpath.twitter;

import com.unifina.data.IFeedRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.service.FeedService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

public class TwitterModule extends AbstractSignalPathModule implements IFeedRequirement {

	public StringParameter keyword = new StringParameter(this, "keyword", "");
	
	public StringOutput tweet = new StringOutput(this, "tweet");
	public StringOutput username = new StringOutput(this, "username");
	public StringOutput name = new StringOutput(this, "name");
	public StringOutput language = new StringOutput(this, "language");
	public TimeSeriesOutput followers = new TimeSeriesOutput(this, "followers");
	public TimeSeriesOutput favorited = new TimeSeriesOutput(this, "favorited");
	public TimeSeriesOutput isRetweet = new TimeSeriesOutput(this, "retweet?");
	public TimeSeriesOutput isReply = new TimeSeriesOutput(this, "reply?");
	
	@Override
	public void init() {
		addInput(keyword);
		
		addOutput(tweet);
		addOutput(username);
		addOutput(name);
		addOutput(language);
		
		addOutput(followers);
		followers.noRepeat = false;
		addOutput(favorited);
		favorited.noRepeat = false;
		addOutput(isRetweet);
		isRetweet.noRepeat = false;
		addOutput(isReply);
		isReply.noRepeat = false;
	}
	
	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

	@Override
	public Feed getFeed() {
		FeedService feedService = (FeedService) globals.getGrailsApplication().getMainContext().getBean("feedService");
		return feedService.getFeedByModule(this);
	}

}
