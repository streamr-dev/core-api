package com.unifina.feed;



public interface MessageSource {
	public void setRecipient(MessageRecipient recipient);
	public void setExpectedCounter(int expected);
}
