package com.unifina.signalpath.twitter;

import com.unifina.signalpath.*;

public class TwitterModule extends AbstractStreamSourceModule {
	
	public StringOutput tweet = new StringOutput(this, "tweet");
	public StringOutput username = new StringOutput(this, "username");
	public StringOutput name = new StringOutput(this, "name");
	public StringOutput language = new StringOutput(this, "language");
	public TimeSeriesOutput followers = new TimeSeriesOutput(this, "followers");
	public BooleanOutput isRetweet = new BooleanOutput(this, "retweet?");

	public ListOutput keywords = new ListOutput(this, "keywords");

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

		addOutput(keywords);
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

}
