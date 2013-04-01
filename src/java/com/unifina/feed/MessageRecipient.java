package com.unifina.feed;


public interface MessageRecipient {
	public void receive(Message message);
	public void sessionBroken();
	public void sessionRestored();
	public void sessionTerminated();
}
