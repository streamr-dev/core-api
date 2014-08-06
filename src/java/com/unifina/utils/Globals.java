package com.unifina.utils;

import groovy.lang.GroovySystem;

import java.security.AccessController;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.datasource.DataSource;
import com.unifina.domain.security.SecUser;
import com.unifina.security.permission.GrailsApplicationPermission;
import com.unifina.security.permission.UserPermission;
import com.unifina.signalpath.AbstractSignalPathModule;

public class Globals {
	
	private static final Logger log = Logger.getLogger(Globals.class);
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
	public boolean abort = false;
	
	private List<Class> dynamicClasses = new ArrayList<>();
	
	protected Date startDate = null;
	protected Date endDate = null;
	
	public Globals(Map signalPathContext, GrailsApplication grailsApplication, SecUser user) {
		if (signalPathContext==null)
			throw new NullPointerException("signalPathContext can not be null!");
			
		if (grailsApplication==null)
			throw new NullPointerException("grailsApplication can not be null!");
		
		this.signalPathContext = signalPathContext;
		this.grailsApplication = grailsApplication;
		this.user = user;
		// Use ModuleService classloader as parent to keep all loaded modules in same CL hierarchy
//		this.classLoader = new ModuleClassLoader(ModuleService.class.getClassLoader())
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
		// Else try to read the auto-detected value from the signalPathContext
		else if (MapTraversal.getString(signalPathContext, "timeOfDayFilter.timeZoneOffset")!=null) {
			String tzOffset = MapTraversal.getString(signalPathContext, "timeOfDayFilter.timeZoneOffset");
			String tzDst = MapTraversal.getString(signalPathContext, "timeOfDayFilter.timeZoneDst");
			tzString = "GMT"+tzOffset;
		}
		else {
			log.info("User time zone info not found, setting it to UTC");
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
		startDate = MapTraversal.getDate(signalPathContext, "beginDate", dateFormat);
		endDate = MapTraversal.getDate(signalPathContext, "endDate", dateFormat);
		time = startDate;
		
		// Set time to midnight UTC of the current date if nothing specified
		if (startDate==null) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			time = cal.getTime();
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
//		for (Class c : classLoader.getLoadedClasses()){
		for (Class c : dynamicClasses) {
			GroovySystem.getMetaClassRegistry().removeMetaClass(c);
//			log.info("destroy(): removed from MetaClassRegistry: $c")
		}
//		classLoader.clearCache() // Just in case
//		classLoader.close()
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
