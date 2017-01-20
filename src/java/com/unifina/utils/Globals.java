package com.unifina.utils;

import com.unifina.datasource.DataSource;
import com.unifina.domain.security.SecUser;
import com.unifina.push.PushChannel;
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

	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd");
	public SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	public SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	protected GrailsApplication grailsApplication;
	
	// TODO: remove, parse everything to real fields
	protected Map signalPathContext = null;
	
	public Date time;
	public HashMap<Object,AbstractSignalPathModule> sharedInstances = new HashMap<>();
	
	private TimeZone userTimeZone;
	private SecUser user;
	
	private TimezoneConverter tzConverter;
	
	protected DataSource dataSource = null;
	
	private List<Class> dynamicClasses = new ArrayList<>();
	
	protected Date startDate = null;
	protected Date endDate = null;
	
	protected PushChannel uiChannel = null;
	protected boolean realtime = false;

	/**
	 * Construct fake environment, e.g., for testing.
	 */
	public Globals() {
		signalPathContext = new HashMap();
	}
	
	public Globals(Map signalPathContext, GrailsApplication grailsApplication, SecUser user) {
		if (signalPathContext==null)
			throw new NullPointerException("signalPathContext can not be null!");
			
		if (grailsApplication==null)
			throw new NullPointerException("grailsApplication can not be null!");
		
		this.signalPathContext = signalPathContext;
		this.grailsApplication = grailsApplication;
		this.user = user;
		
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public void onModuleCreated(AbstractSignalPathModule module) {

	}
	
	public void onModuleInitialized(AbstractSignalPathModule module) {

	}
	
	public Map getSignalPathContext() {
		return signalPathContext;
	}
	
	// TODO: risky to keep these here, should be out of sight of user code
	public GrailsApplication getGrailsApplication() {
		if (System.getSecurityManager()!=null)
			AccessController.checkPermission(new GrailsApplicationPermission());
		
		return grailsApplication;
	}


	// TODO: risky to keep these here, should be out of sight of user code
	/**
	 * Returns the SecUser for this Globals instance, or null if the user is anonymous/unknown.
     */
	public SecUser getUser() {
		if (System.getSecurityManager()!=null)
			AccessController.checkPermission(new UserPermission());
		return user;
	}
	
	// TODO: risky to keep these here, should be out of sight of user code
	public void setUser(SecUser user) {
		if (System.getSecurityManager()!=null)
			AccessController.checkPermission(new UserPermission());
		this.user = user;
	}
	
	protected String detectTimeZone() {
		String tzString;
		
		if (user!=null)
			tzString = user.getTimezone();
		else {
			log.info("User not found, setting time zone to UTC");
			tzString = "UTC";
		}

		return tzString;
	}
	
	protected void initTimeZone(String tzString) {
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

			// Set time to midnight UTC of the current date if nothing specified
			if (startDate==null) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(new Date());
				cal.set(Calendar.HOUR_OF_DAY,0);
				cal.set(Calendar.MINUTE,0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND,0);
				time = cal.getTime();
			} else {
				time = startDate;
			}

			// Interpret endDate as one millisecond to the next midnight
			// Change this if the possibility to enter a time range is added
			endDate = MapTraversal.getDate(signalPathContext, "endDate", dateFormatUTC);
			if (endDate!=null) {
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
		return userTimeZone;
	}
	
	public void setUserTimeZone(TimeZone tz) {
		this.userTimeZone = tz;
		timeFormat.setTimeZone(tz);
	}
	
	public void registerDynamicClass(Class c) {
		dynamicClasses.add(c);
	}
	
	public void destroy() {
		for (Class c : dynamicClasses) {
			GroovySystem.getMetaClassRegistry().removeMetaClass(c);
		}
		if (uiChannel!=null)
			uiChannel.destroy();
	}

	public void setRealtime(boolean realtime) {
		this.realtime = realtime;
	}

	public boolean isRealtime() {
		return realtime;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public PushChannel getUiChannel() {
		return uiChannel;
	}

	public void setUiChannel(PushChannel uiChannel) {
		this.uiChannel = uiChannel;
	}

	public void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	public <T> T getBean(Class<T> requiredType) {
		return grailsApplication.getMainContext().getBean(requiredType);
	}

	/**
	 * Returns true if we are about to run something, and not eg. reconstructing canvases or something like that.
	 * Currently returns true if the DataSource is set.
     */
	public boolean isRunContext() {
		return getDataSource() != null;
	}
}
