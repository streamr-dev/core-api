package com.unifina.data;

import java.util.TimeZone;



public interface IFeed {
	
	public boolean subscribe(Object object) throws Exception;
	
	public void startFeed() throws Exception;
	public void stopFeed() throws Exception;
	
	public void setTimeZone(TimeZone tz);
	public void setEventQueue(IEventQueue queue);
	
}
