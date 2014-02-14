package com.unifina.feed;

import java.util.Date;

import com.unifina.data.IBacktestFeed;
import com.unifina.utils.Globals;

public abstract class AbstractHistoricalFeed extends AbstractFeed implements
		IBacktestFeed {

	protected Date beginDate;
	protected Date endDate;
	
	public AbstractHistoricalFeed(Globals globals) {
		super(globals);
		
		// If the beginDate and endDate are not set, try to read them from Globals
		// Also possible to set them programmatically via setBeginDate() and setEndDate(), used in eg. ChartService
		beginDate = globals.getStartDate();
		endDate = globals.getEndDate();
	}

	@Override
	public void setBeginDate(Date date) {
		this.beginDate = date;
	}

	@Override
	public void setEndDate(Date date) {
		this.endDate = date;
	}

}
