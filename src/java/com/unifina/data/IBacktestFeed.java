package com.unifina.data;
import java.util.Date;
import java.util.List;

/**
 * BacktestFeeds are feeds that can be polled for more events, ie.
 * they know the next events in advance.
 * @author Henri
 *
 */
// TODO: remove this class completely and just use AbstractHistoricalFeed
@Deprecated
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

	 * @param beginDate Earliest unit start datetime than can be returned
	 * @param endDate Latest unit end datetime that can be returned
	 * @return
	 */
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception;

}
