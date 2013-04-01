package com.unifina.datasource;

import java.util.Date;

public interface IDayListener {
	/**
	 * The DataSource calls this method in the beginning of each new day.
	 * @param day
	 */
	public void onDay(Date day);
}
