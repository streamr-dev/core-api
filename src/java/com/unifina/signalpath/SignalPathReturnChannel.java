package com.unifina.signalpath;

import grails.converters.JSON;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy.Builder;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicyListener;
import org.codehaus.groovy.grails.web.pages.FastStringWriter;

import com.unifina.atmosphere.CounterBroadcasterCache;

public class SignalPathReturnChannel extends Thread implements IReturnChannel, BroadcasterLifeCyclePolicyListener {

//    private static final ExecutorService broadcastExecutorService = Executors.newCachedThreadPool();
//    private static final ExecutorService broadcastAsyncExecutorService = Executors.newCachedThreadPool();
	
	int payloadCounter = 0;
	
	public Broadcaster bc;
	
	ServletContext sc;
	String sessionId;
	String channel;
	
	SignalPath signalPath;
	
	boolean destroyOnEmpty = false;
	boolean destroyOnIdle = false;
	
	int MAX_MSG = Integer.MAX_VALUE;
	
	private final static String EOM = ",";
	
	CounterBroadcasterCache cache;
	
	private static final Logger log = Logger.getLogger(SignalPathReturnChannel.class);
	
	Map<String,SignalPathReturnChannel> returnChannels;
	
	JSONWithEOMAppender json = new JSONWithEOMAppender();
	
	ArrayDeque<SignalPathMessage> queue = new ArrayDeque<>();

	private boolean abort = false;
	
	public SignalPathReturnChannel(String sessionId, String channel, ServletContext sc) {
		this.sessionId = sessionId;
		this.channel = channel;
		this.sc = sc;
		
		bc = BroadcasterFactory.getDefault().lookup(sessionId, true);
		
//		bc.getBroadcasterConfig().setExecutorService(broadcastExecutorService, true);
//		bc.getBroadcasterConfig().setAsyncWriteService(broadcastAsyncExecutorService, true);
		
		bc.setBroadcasterLifeCyclePolicy(new Builder().policy(ATMOSPHERE_RESOURCE_POLICY.IDLE_DESTROY).idleTime(60*12, TimeUnit.MINUTES).build());
		bc.addBroadcasterLifeCyclePolicyListener(this);

		cache = (CounterBroadcasterCache) bc.getBroadcasterConfig().getBroadcasterCache();
		
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
			// Don't increment payloadCounter until after successful json conversion
			int c = payloadCounter;
			msg.setCounter(c);
			String s = msg.toJSONString();
			payloadCounter++;
			
			cache.add(s, c, msg.cacheId);
			bc.broadcast(s);
		} catch (Exception e) {
			log.error("Error broadcasting message: "+msg, e);
		}
	}
	
	
	@Override
	public synchronized void sendDone() {
		queue.add(new DoneMessage());
//		broadcast(new DoneMessage(payloadCounter++));
		notify();
	}

	@Override
	public synchronized void sendError(String err) {
		queue.add(new ErrorMessage(err));
//		broadcast(new ErrorMessage(payloadCounter++, err));
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
//		broadcast(new PayloadMessage(payloadCounter++, hash, p));
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
//		broadcast(new PayloadMessage(payloadCounter++, hash, p, identifier));
		notify();
	}
	
	public synchronized void destroy() {
		bc.destroy();
		
		abort = true;
		interrupt();
		notify();
	}
	
	void cleanUp() {
		log.info("Cleaning up response channel for session "+sessionId);
		returnChannels.remove(sessionId);

		log.info("Remaining return channels:");
		for (SignalPathReturnChannel c : returnChannels.values())
			log.info(c.sessionId+" broadcaster destroyed: "+c.bc.isDestroyed());
	}
	
	/**
	 * Broadcaster lifecycle listener implementation: clean up this class when the broadcaster is destroyed
	 */
	public void onEmpty() {
		if (destroyOnEmpty)
			destroy();
	}
	public void onIdle() {}
	public void onDestroy() {
		cleanUp();
	}
	
	@Override
	public String toString() {
		return "SignalPathReturnChannel: "+sessionId;
	}

	class JSONWithEOMAppender extends JSON {
	    @Override
	    public String toString() {
	        FastStringWriter writer = new FastStringWriter();
	        try {
	            render(writer);
	            writer.write(EOM);
	        }
	        catch (Exception e) {
	            throw new UnhandledException(e);
	        }
	        return writer.toString();
	    }
	}
	
	class SignalPathMessage extends LinkedHashMap<String,Object>{
		public Object cacheId = null;
		public int counter;
		
		public SignalPathMessage() {
//			this.put("counter",counter);
		}
		
		public void setCounter(int counter) {
			this.counter = counter;
			this.put("counter",counter);
		}
		
		public String toJSONString() {
			json.setTarget(this);
			return json.toString();
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
}
