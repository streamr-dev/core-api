package com.unifina.data;

import java.util.TimeZone;


/**
 * A lightweight feed implementation interface. In most cases you will
 * want to extend AbstractFeed ir AbstractHistoricalFeed instead of directly
 * implementing this interface. For implementing historical feeds implementing 
 * this interface is fine.
 * 
 * Deprecation: Just use AbstractFeed as top of the hierarchy
 * @author Henri
 *
 */
@Deprecated
public interface IFeed {
	
	public boolean subscribe(Object object) throws Exception;
	
	public void startFeed() throws Exception;
	public void stopFeed() throws Exception;
	
	public void setTimeZone(TimeZone tz);
	public void setEventQueue(IEventQueue queue);
	
}
