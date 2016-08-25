package com.unifina.signalpath.twitter;

import com.unifina.signalpath.AbstractStreamSourceModule;
import com.unifina.signalpath.BooleanOutput;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.TimeSeriesOutput;

public class TwitterModule extends AbstractStreamSourceModule {
	
	public StringOutput tweet = new StringOutput(this, "tweet");
	public StringOutput username = new StringOutput(this, "username");
	public StringOutput name = new StringOutput(this, "name");
	public StringOutput language = new StringOutput(this, "language");
	public TimeSeriesOutput followers = new TimeSeriesOutput(this, "followers");
	public BooleanOutput isRetweet = new BooleanOutput(this, "retweet?");
	
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
	}
	
	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

}
