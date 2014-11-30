package com.unifina.push;

import grails.converters.JSON;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy.Builder;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicyListener;
import org.codehaus.groovy.grails.web.pages.FastStringWriter;

import com.unifina.atmosphere.CounterBroadcasterCache;
import com.unifina.signalpath.SignalPathReturnChannel;

public class AtmospherePushChannel extends PushChannel implements BroadcasterLifeCyclePolicyListener {
	
	private Broadcaster bc;
	private CounterBroadcasterCache cache;
	private JSON json = new JSONWithEOMAppender();
	
	private final static String EOM = ",";
	
	private boolean destroyOnEmpty = false;
	private boolean destroyOnIdle = false;
	
	private static final Logger log = Logger.getLogger(AtmospherePushChannel.class);
	
	public AtmospherePushChannel(String channel) {
		super(channel);
		
		bc = BroadcasterFactory.getDefault().lookup(channel, true);
		
		bc.setBroadcasterLifeCyclePolicy(new Builder().policy(ATMOSPHERE_RESOURCE_POLICY.IDLE_DESTROY).idleTime(60*12, TimeUnit.MINUTES).build());
		bc.addBroadcasterLifeCyclePolicyListener(this);

		cache = (CounterBroadcasterCache) bc.getBroadcasterConfig().getBroadcasterCache();
	}

	public Broadcaster getBroadcaster() {
		return bc;
	}
	
	@Override
	protected void doPush(PushChannelMessage msg) {
		String s = msg.toJSON(json);
		cache.add(s, msg.getCounter(), (msg.getContent() instanceof SignalPathReturnChannel.SignalPathMessage ? ((SignalPathReturnChannel.SignalPathMessage) msg.getContent()).cacheId : null));
		bc.broadcast(s);
	}
	
	/**
	 * Broadcaster lifecycle listener implementation: clean up this class when the broadcaster is destroyed
	 */
	@Override
	public void onEmpty() {
		if (destroyOnEmpty)
			destroy();
	}
	
	@Override
	public void onIdle() {
		if (destroyOnIdle)
			destroy();
	}
	
	@Override
	public void onDestroy() {
		log.info("Atmosphere Broadcaster for "+getChannel()+" destroyed.");
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

	@Override
	public void destroy() {
		super.destroy();
		bc.destroy();
	}

}
