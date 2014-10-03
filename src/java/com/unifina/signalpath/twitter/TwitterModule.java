package com.unifina.signalpath.twitter;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StreamParameter;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.TimeSeriesOutput;

public class TwitterModule extends AbstractSignalPathModule implements IStreamRequirement {

	public StreamParameter stream = new StreamParameter(this, "stream");
	
	public StringOutput tweet = new StringOutput(this, "tweet");
	public StringOutput username = new StringOutput(this, "username");
	public StringOutput name = new StringOutput(this, "name");
	public StringOutput language = new StringOutput(this, "language");
	public TimeSeriesOutput followers = new TimeSeriesOutput(this, "followers");
	public TimeSeriesOutput isRetweet = new TimeSeriesOutput(this, "retweet?");
	public TimeSeriesOutput isReply = new TimeSeriesOutput(this, "reply?");
	
	@Override
	public void init() {
		addInput(stream);
		
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

	@Override
	public Stream getStream() {
		return stream.getValue();
	}

}
