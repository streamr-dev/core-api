package com.unifina.signalpath;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.unifina.push.PushChannel;
import com.unifina.push.SocketIOPushChannel;

public class SignalPathReturnChannel extends Thread implements IReturnChannel {
	
	private ServletContext sc;
	private String sessionId;
	private SignalPath signalPath;
	
	int MAX_MSG = Integer.MAX_VALUE;
	
	private static final Logger log = Logger.getLogger(SignalPathReturnChannel.class);
	
	Map<String,SignalPathReturnChannel> returnChannels;
	
	ArrayDeque<SignalPathMessage> queue = new ArrayDeque<>();

	private boolean abort = false;
	
	private PushChannel pushChannel;
	
	public SignalPathReturnChannel(String sessionId, String channel, ServletContext sc) {
		this.sessionId = sessionId;
		this.sc = sc;

		//pushChannel = new AtmospherePushChannel(channel);
		pushChannel = new SocketIOPushChannel(channel);
		
		// Associate session with return channel
		returnChannels = (Map<String,SignalPathReturnChannel>) sc.getAttribute("returnChannels");
		
		if (returnChannels==null) {
			returnChannels = new ConcurrentHashMap<String,SignalPathReturnChannel>();
			sc.setAttribute("returnChannels",returnChannels);
		}
		
		returnChannels.put(sessionId,this);
		this.start();
	}
	
	@Override
	public void run() {
		log.info("SignalPathReturnChannel "+sessionId+" starting.");
		while(!abort) {
			synchronized(this) {
				if (queue.isEmpty())
					try {
						wait();
					} catch (InterruptedException e) {
						if (abort) {
							log.info("Session "+sessionId+" interrupted.");
							break;
						}
					}
				else broadcast(queue.poll());
			}
		}
		log.info("SignalPathReturnChannel "+sessionId+" quitting.");
	}
	
	private void broadcast(SignalPathMessage msg) {
		try {
			pushChannel.push(msg);
		} catch (Exception e) {
			log.error("Error broadcasting message: "+msg, e);
		}
	}
	
	
	@Override
	public synchronized void sendDone() {
		queue.add(new DoneMessage());
		notify();
	}

	@Override
	public synchronized void sendError(String err) {
		queue.add(new ErrorMessage(err));
		notify();
	}
	
	@Override
	public synchronized void sendNotification(String msg) {
		queue.add(new NotificationMessage(msg));
		notify();
	}
	
	@Override
	public synchronized void sendPayload(int hash, Object p) {
		queue.add(new PayloadMessage(hash, p));
		notify();
	}

	public synchronized void sendRawMessage(Map<String,Object> map, Object cacheId) {
		SignalPathMessage msg = new SignalPathMessage();
		for (String s : map.keySet())
			msg.put(s,map.get(s));
		msg.cacheId = cacheId;
		queue.add(msg);
		notify();
	}
	
	@Override
	public synchronized void sendReplacingPayload(int hash, Object p, Object identifier) {
		queue.add(new PayloadMessage(hash, p, identifier));
		notify();
	}
	
	public synchronized void destroy() {
		pushChannel.destroy();
		abort = true;
		interrupt();
		notify();
	}
	
	void cleanUp() {
		log.info("Cleaning up response channel for session "+sessionId);
		returnChannels.remove(sessionId);

		log.info("Remaining return channels:");
		for (SignalPathReturnChannel c : returnChannels.values())
			log.info(c.sessionId+" broadcaster destroyed: "+c.getPushChannel().isDestroyed());
	}
	
	@Override
	public String toString() {
		return "SignalPathReturnChannel: "+sessionId;
	}
	
	public PushChannel getPushChannel() {
		return pushChannel;
	}
	
	public class SignalPathMessage extends LinkedHashMap<String,Object>{
		public Object cacheId = null;
		public int counter;
		
		public SignalPathMessage() {
//			this.put("counter",counter);
		}
		
		public void setCounter(int counter) {
			this.counter = counter;
			this.put("counter",counter);
		}

	}
	
	class DoneMessage extends SignalPathMessage {
		public DoneMessage() {
			this.put("type","D");
		}
	}
	
	class ErrorMessage extends SignalPathMessage {
		public ErrorMessage(String error) {
			this.put("type","E");
			this.put("error",error);
			this.cacheId = "error";
		}
	}
	
	class NotificationMessage extends SignalPathMessage {
		public NotificationMessage(String msg) {
			this.put("type","N");
			this.put("msg",msg);
			this.cacheId = "notification";
		}
	}
	
	class PayloadMessage extends SignalPathMessage {
		public PayloadMessage(int hash, Object payload) {
			this.put("type","P");
			this.put("hash",hash);
			this.put("payload", payload);
		}
		
		public PayloadMessage(int hash, Object payload, Object cacheId) {
			this.put("type","P");
			this.put("hash",hash);
			this.put("payload", payload);
			this.cacheId = cacheId;
		}
	}

	@Override
	public SignalPath getSignalPath() {
		return signalPath;
	}

	public void setSignalPath(SignalPath signalPath) {
		this.signalPath = signalPath;
	}
}
