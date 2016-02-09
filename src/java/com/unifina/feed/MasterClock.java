package com.unifina.feed;

import java.util.Date;

import com.unifina.data.FeedEvent;
import com.unifina.datasource.DataSource;
import com.unifina.datasource.ITimeListener;
import com.unifina.utils.Globals;

public class MasterClock extends AbstractEventRecipient<ITimeListener, ITimestamped> {
	int i;

	public MasterClock(Globals globals, DataSource dataSource) {
		super(globals);
		// globals.dataSource is not yet set
		dataSource.addStartListener(this);
	}
	
	protected void sendOutputFromModules(FeedEvent event) {
		Date d = event.timestamp;
		
		// Don't use iterators to prevent ConcurrentModificationException in case new timelisteners are added
		for (i=0;i<moduleSize;i++)
			modules.get(i).setTime(d);
	}
}
