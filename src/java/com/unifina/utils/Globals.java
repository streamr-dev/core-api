package com.unifina.utils;

import com.unifina.datasource.DataSource;
import com.unifina.domain.security.SecUser;
import com.unifina.security.permission.DataSourcePermission;

import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class Globals {
	private final SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private final Map signalPathContext;
	private final Long userId;
	private final TimeZone userTimeZone;
	private final TimezoneConverter tzConverter;
	private DataSource dataSource = null;
	private Date startDate = null;
	private Date endDate = null;
	private boolean realtime = false;
	private IdGenerator idGenerator = new IdGenerator();

	public Date time;

	/**
	 * Construct fake environment, e.g., for testing.
	 */
	public Globals() {
		this(new HashMap(), null);
	}
	
	public Globals(Map signalPathContext, SecUser user) {
		if (signalPathContext == null) {
			throw new NullPointerException("signalPathContext can not be null!");
		}
		
		this.signalPathContext = signalPathContext;
		this.userId = user != null ? user.getId() : null;
		
		String tzString = resolveTimezoneString(user);
		this.userTimeZone = TimeZone.getTimeZone(tzString);
		this.tzConverter = new TimezoneConverter(tzString);
		this.dateTimeFormat.setTimeZone(this.userTimeZone);
		this.dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public Map getSignalPathContext() {
		return signalPathContext;
	}

	public Long getUserId() {
		return userId;
	}

	public Date getTime() {
		return time;
	}
	
	public void init() {
		try {
			time = startDate = new Date(MapTraversal.getLong(signalPathContext, "beginDate"));
			endDate = new Date(MapTraversal.getLong(signalPathContext, "endDate"));
		} catch (NumberFormatException | NullPointerException e) {
			// TODO: remove this fallback handling in favor of Long dates in future.
			// Use UTC timezone for beginDate and endDate
			startDate = MapTraversal.getDate(signalPathContext, "beginDate", dateFormatUTC);

			if (isRealtime()) {
				time = new Date();
			} else if (startDate!=null) {
				time = startDate;
			} else {
				// As a fallback, set time to midnight today
				time = TimeOfDayUtil.getMidnight(new Date());
			}

			// Interpret endDate as one millisecond to the next midnight
			// Change this if the possibility to enter a time range is added
			endDate = MapTraversal.getDate(signalPathContext, "endDate", dateFormatUTC);
			if (endDate != null) {
				endDate = new Date(TimeOfDayUtil.getMidnight(endDate).getTime() + 24 * 60 * 60 * 1000 - 1);
			}
		}
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public TimezoneConverter getTzConverter() {
		return tzConverter;
	}
	
	public TimeZone getUserTimeZone() {
		return userTimeZone;
	}

	public void setRealtime(boolean realtime) {
		this.realtime = realtime;
	}

	public boolean isRealtime() {
		return realtime;
	}

	public boolean isAdhoc() {
		return !isRealtime();
	}

	public boolean isSerializationEnabled() {
		return MapTraversal.getBoolean(signalPathContext, "serializationEnabled");
	}
	
	public DataSource getDataSource() {
		if (System.getSecurityManager() != null) {
			AccessController.checkPermission(new DataSourcePermission());
		}
		return dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		if (System.getSecurityManager() != null) {
			AccessController.checkPermission(new DataSourcePermission());
		}
		this.dataSource = dataSource;
	}

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	/**
	 * Returns true if we are about to run something, and not eg. reconstructing canvases or something like that.
	 * Currently returns true if the DataSource is set.
     */
	public boolean isRunContext() {
		return getDataSource() != null;
	}

	public String formatDateTime(Date date) {
		return dateTimeFormat.format(date);
	}

	private static String resolveTimezoneString(SecUser user) {
		return user != null && user.getTimezone() != null ? user.getTimezone() : "UTC";
	}
}
