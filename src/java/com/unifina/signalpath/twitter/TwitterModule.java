package com.unifina.signalpath.twitter;

import com.unifina.signalpath.AbstractStreamSourceModule;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.TimeSeriesOutput;

public class TwitterModule extends AbstractStreamSourceModule {
	
	public StringOutput tweet = new StringOutput(this, "tweet");
	public StringOutput username = new StringOutput(this, "username");
	public StringOutput name = new StringOutput(this, "name");
	public StringOutput language = new StringOutput(this, "language");
	public TimeSeriesOutput followers = new TimeSeriesOutput(this, "followers");
	public TimeSeriesOutput isRetweet = new TimeSeriesOutput(this, "retweet?");
	public TimeSeriesOutput isReply = new TimeSeriesOutput(this, "reply?");
	
	@Override
	public void init() {
		super.init();
		
		addOutput(tweet);
		addOutput(username);
		addOutput(name);
		addOutput(language);
		
		addOutput(followers);
		followers.noRepeat = false;
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

}
