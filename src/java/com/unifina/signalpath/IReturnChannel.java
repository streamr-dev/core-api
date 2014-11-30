package com.unifina.signalpath;

@Deprecated // Unnecessary, replace with SignalPathReturnChannel or whatever replaces it
public interface IReturnChannel {
	/**
	 * Sends a payload via the return channel
	 * @param hash Id of the recipient module
	 * @param payload Payload to be sent
	 */
	public void sendPayload(int hash, Object payload);
	
	/**
	 * Sends a replacement payload via the return channel. Any previous
	 * message with the same identifier object is purged from the cache.
	 * @param hash Id of the recipient module
	 * @param payload Payload to be sent
	 * @param identifier An object identifying the category in which the previous message will be replaced.
	 */
	public void sendReplacingPayload(int hash, Object payload, Object identifier);
	
	public void sendDone();
	
	public void sendNotification(String msg);
	
	public void sendError(String errorMsg);
	
	public SignalPath getSignalPath();
	
	public void destroy();
	
//	public void setTransportReady(boolean ready, boolean setToFalseOnBroadcast);
}
