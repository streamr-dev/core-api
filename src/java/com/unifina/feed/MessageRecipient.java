package com.unifina.feed;


public interface MessageRecipient<RawMessageClass, KeyClass> {
	public void receive(Message<RawMessageClass, KeyClass> message);
	public void sessionBroken();
	public void sessionRestored();
	public void sessionTerminated();
	public int getReceivePriority(); // not getPriority because many MessageRecipients are Threads, which defines getPriority
}
