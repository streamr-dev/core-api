package com.unifina.utils;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.HistoricalDataSource;
import com.unifina.datasource.RealtimeDataSource;
import com.unifina.domain.security.SecUser;
import com.unifina.security.permission.DataSourcePermission;

import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class Globals {

	public enum Mode {
		REALTIME,
		HISTORICAL,
		NOT_PLANNING_TO_RUN
	}

	private final SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd");

	private final Map signalPathContext;
	private final Long userId;
	private final DataSource dataSource;
	private final Mode mode;
	private Date startDate;
	private Date endDate;
	private IdGenerator idGenerator = new IdGenerator();

	public Date time;

	/**
	 * Creates an "empty" Globals with no settings, no user, Mode.NOT_PLANNING_TO_RUN, and no DataSource.
	 */
	public Globals() {
		this(new HashMap(), null, Mode.NOT_PLANNING_TO_RUN, null);
	}

	/**
	 * Defaults to Mode.NOT_PLANNING_TO_RUN, no DataSource is created
	 */
	public Globals(Map signalPathContext, SecUser user) {
		this(signalPathContext, user, Mode.NOT_PLANNING_TO_RUN, null);
	}

	/**
	 * Will create DataSource based on Mode
	 */
	public Globals(Map signalPathContext, SecUser user, Mode mode) {
		this(signalPathContext, user, mode, null);
	}

	/**
	 * Allows a DataSource implementation to be passed explicitly. If it is null,
	 * a DataSource based on given Mode will be automatically created.
	 */
	public Globals(Map signalPathContext, SecUser user, Mode mode, DataSource dataSource) {
		if (signalPathContext == null) {
			throw new NullPointerException("signalPathContext can not be null!");
		}
		this.signalPathContext = signalPathContext;
		this.userId = user != null ? user.getId() : null;
		this.dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.mode = mode;

		if (mode.equals(Mode.REALTIME)) {
			time = new Date();
		} else if (mode.equals(Mode.HISTORICAL)) {
			// Parse startDate and endDate
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
					time = DateRange.getMidnight(new Date());
				}

				// Interpret endDate as one millisecond to the next midnight
				// Change this if the possibility to enter a time range is added
				endDate = MapTraversal.getDate(signalPathContext, "endDate", dateFormatUTC);
				if (endDate != null) {
					endDate = new Date(DateRange.getMidnight(endDate).getTime() + 24 * 60 * 60 * 1000 - 1);
				}
			}


		}

		if (dataSource == null && !mode.equals(Mode.NOT_PLANNING_TO_RUN)) {
			this.dataSource = isRealtime() ? new RealtimeDataSource(this) : new HistoricalDataSource(this);
		} else {
			this.dataSource = dataSource;
		}

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

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public boolean isRealtime() {
		return mode.equals(Mode.REALTIME);
	}

	public boolean isAdhoc() {
		return mode.equals(Mode.HISTORICAL);
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

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	/**
	 * Returns true if the mode is REALTIME or HISTORICAL, i.e. we are about to run something,
	 * and not reconstructing canvases or something like that.
     */
	public boolean isRunContext() {
		return !mode.equals(Mode.NOT_PLANNING_TO_RUN);
	}
}
