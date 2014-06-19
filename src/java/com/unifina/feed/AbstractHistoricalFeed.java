package com.unifina.feed;

import com.unifina.data.IBacktestFeed;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

// TODO: Is this class layer needed?
public abstract class AbstractHistoricalFeed extends AbstractFeed implements
		IBacktestFeed {
	
	public AbstractHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

}
