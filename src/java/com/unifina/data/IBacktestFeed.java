package com.unifina.data;
import java.util.Date;
import java.util.List;

/**
 * BacktestFeeds are feeds that can be polled for more events, ie.
 * they know the next events in advance.
 * @author Henri
 *
 */
public interface IBacktestFeed extends IFeed {

	/**
	 * This method is used to poll events from the backtest feed.
	 * It should return null if and only if the feed has been depleted,
	 * ie. there are no more events.
	 * @return The next FeedEvent, or null if there is none
	 * @throws Exception
	 */
	public FeedEvent getNext() throws Exception;
	
	/**
	 * Should return a list of date pairs, where date[0] represents unit
	 * start datetime and date[1] represents unit end datetime.
	 * @param backtest Backtest for which the units are for.
	 * @return
	 */
	public List<Date[]> getBacktestUnits(Object backtest) throws Exception;
	
	public void setBeginDate(Date date);
	public void setEndDate(Date date);
}
