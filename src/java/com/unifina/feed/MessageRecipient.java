package com.unifina.feed;


public interface MessageRecipient {
	public void receive(Message message);
	public void sessionBroken();
	public void sessionRestored();
	public void sessionTerminated();
	public int getReceivePriority(); // not getPriority because many MessageRecipients are Threads, which defines getPriority
}
