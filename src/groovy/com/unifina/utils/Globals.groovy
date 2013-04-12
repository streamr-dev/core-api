package com.unifina.utils

import java.text.SimpleDateFormat

import com.unifina.datasource.DataSource
import com.unifina.datasource.IStopListener;
import com.unifina.security.SecUser
import com.unifina.signalpath.AbstractSignalPathModule

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication

public class Globals {
	
	private static final Logger log = Logger.getLogger(Globals.class)
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	public SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	GrailsApplication grailsApplication
	
	// TODO: remove, parse everything to real fields
	private Map signalPathContext = null
	
	public Date time
	public HashMap<Object,AbstractSignalPathModule> sharedInstances = new HashMap<>()
	
	TimeZone userTimeZone
	SecUser user
	
	TimezoneConverter tzConverter
	GroovyClassLoader groovyClassLoader
	
	private DataSource dataSource = null
	public boolean abort = false
	
	public Globals(Map signalPathContext, GrailsApplication grailsApplication, SecUser user) {
		if (signalPathContext==null)
			throw new NullPointerException("signalPathContext can not be null!")
			
		if (grailsApplication==null)
			throw new NullPointerException("grailsApplication can not be null!")
		
		this.signalPathContext = signalPathContext;
		this.grailsApplication = grailsApplication;
		this.user = user
		this.groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader())
	}
	
	public void onModuleCreated(AbstractSignalPathModule module) {

	}
	
	public void onModuleInitialized(AbstractSignalPathModule module) {

	}
	
	public Map getSignalPathContext() {
		return signalPathContext
	}
	
	protected String detectTimeZone() {
		String tzString
		
		if (user!=null)
			tzString = user.timezone
		// Else try to read the auto-detected value from the signalPathContext
		else if (signalPathContext.timeOfDayFilter?.timeZoneOffset) {
			String tzOffset = signalPathContext.timeOfDayFilter.timeZoneOffset
			String tzDst = signalPathContext.timeOfDayFilter.timeZoneDst
			tzString = "GMT"+tzOffset
		}
		else {
			log.info("User time zone info not found, setting it to UTC")
			tzString = "UTC"
		}

		return tzString		
	}
	
	protected String initTimeZone(String tzString) {
		TimeZone tz = TimeZone.getTimeZone(tzString)
		
		dateFormat.setTimeZone(tz)
		timeFormat.setTimeZone(tz)
		dateTimeFormat.setTimeZone(tz)
		
		this.userTimeZone = tz
		
		tzConverter = new TimezoneConverter(tzString)
	}
	
	public void init() {
		// Set time to midnight UTC of the current date
		if (signalPathContext.beginDate) {
			time = dateFormat.parse(signalPathContext.beginDate)
		}
		else {
			Calendar cal = new GregorianCalendar()
			cal.setTime(new Date())
			cal.set(Calendar.HOUR_OF_DAY,0)
			cal.set(Calendar.MINUTE,0)
			cal.set(Calendar.SECOND,0)
			cal.set(Calendar.MILLISECOND,0)
			time = cal.getTime()
		}
		
		String tzString = detectTimeZone()
		initTimeZone(tzString)
	}
	
	public Date getStartDate() {
		return signalPathContext.startDate
	}
	
	public Date getEndDate() {
		return signalPathContext.endDate
	}
	
	public TimeZone getUserTimeZone() {
		return userTimeZone
	}
	
	public void setUserTimeZone(TimeZone tz) {
		this.userTimeZone = tz
		timeFormat.setTimeZone(tz)
	}
	
	public void destroy() {
		for (Class c : groovyClassLoader.getLoadedClasses()){
			GroovySystem.getMetaClassRegistry().removeMetaClass(c);
//			log.info("destroy(): removed from MetaClassRegistry: $c")
		}
	}
	
	public DataSource getDataSource() {
		return dataSource
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource
	}

}
