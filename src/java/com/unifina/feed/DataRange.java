package com.unifina.feed;

import java.util.Date;

/**
 * Created by henripihkala on 25/02/16.
 */
public class DataRange {

	private final Date beginDate;
	private final Date endDate;

	public DataRange(Date beginDate, Date endDate) {
		this.beginDate = beginDate;
		this.endDate = endDate;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

}
