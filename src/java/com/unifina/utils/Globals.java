package com.unifina.utils;

import com.unifina.datasource.DataSource;
import com.unifina.domain.security.SecUser;
import com.unifina.security.permission.DataSourcePermission;
import com.unifina.security.permission.GrailsApplicationPermission;
import com.unifina.security.permission.UserPermission;
import com.unifina.signalpath.AbstractSignalPathModule;
import groovy.lang.GroovySystem;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.*;


public class Globals {
	private static final Logger log = Logger.getLogger(Globals.class);

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private GrailsApplication grailsApplication;
	private Map signalPathContext = null;
	private TimeZone userTimeZone;
	private SecUser user;
	private TimezoneConverter tzConverter;
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
		signalPathContext = new HashMap();
	}
	
	public Globals(Map signalPathContext, GrailsApplication grailsApplication, SecUser user) {
		if (signalPathContext == null) {
			throw new NullPointerException("signalPathContext can not be null!");
		}
		if (grailsApplication == null) {
			throw new NullPointerException("grailsApplication can not be null!");
		}
		
		this.signalPathContext = signalPathContext;
		this.grailsApplication = grailsApplication;
		this.user = user;
		
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public Map getSignalPathContext() {
		return signalPathContext;
	}

	public GrailsApplication getGrailsApplication() {
		if (System.getSecurityManager() != null) { // Ensure cannot be accessed by CustomModule
			AccessController.checkPermission(new GrailsApplicationPermission());
		}
		return grailsApplication;
	}

	/**
	 * Returns the SecUser for this Globals instance, or null if the user is anonymous/unknown.
     */
	public SecUser getUser() {
		if (System.getSecurityManager() != null) { // Ensure cannot be accessed by CustomModule
			AccessController.checkPermission(new UserPermission());
		}
		return user;
	}

	public void setUser(SecUser user) {
		if (System.getSecurityManager() != null) { // Ensure cannot be accessed by CustomModule
			AccessController.checkPermission(new UserPermission());
		}
		this.user = user;
	}

	public Date getTime() {
		return time;
	}
	
	private String detectTimeZone() {
		String tzString;
		
		if (user!=null)
			tzString = user.getTimezone();
		else {
			log.info("User not found, setting time zone to UTC");
			tzString = "UTC";
		}

		return tzString;
	}
	
	private void initTimeZone(String tzString) {
		TimeZone tz = TimeZone.getTimeZone(tzString);
		
		dateFormat.setTimeZone(tz);
		timeFormat.setTimeZone(tz);
		dateTimeFormat.setTimeZone(tz);
		
		this.userTimeZone = tz;
		
		tzConverter = new TimezoneConverter(tzString);
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

		String tzString = detectTimeZone();
		initTimeZone(tzString);
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
		if (userTimeZone == null) {
			userTimeZone = TimeZone.getTimeZone(user.getTimezone());
		}
		return userTimeZone;
	}
	
	public void setUserTimeZone(TimeZone tz) {
		this.userTimeZone = tz;
		timeFormat.setTimeZone(tz);
	}
	
	public void destroy() {
		// TODO: anything needed here?
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
}
