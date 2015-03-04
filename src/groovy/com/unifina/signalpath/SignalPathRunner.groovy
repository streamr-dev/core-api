package com.unifina.signalpath

import grails.util.GrailsUtil

import javax.servlet.ServletContext

import org.apache.log4j.Logger

import com.unifina.push.IHasPushChannel
import com.unifina.service.SignalPathService
import com.unifina.utils.Globals


/**
 * A Thread that instantiates and runs a list of SignalPaths.
 * Identified by a runnerId, by which this runner can be looked up from the
 * servletContext["signalPathRunners"] map.
 */
public class SignalPathRunner extends Thread {
	
	private List<Map> signalPathData
	private final List<SignalPath> signalPaths = Collections.synchronizedList([])
	private boolean deleteOnStop
	
	String runnerId
	
	Globals globals
	
	ServletContext servletContext
	SignalPathService signalPathService
	
	// Have the SignalPaths been instantiated?
	boolean ready = false
	
	private static final Logger log = Logger.getLogger(SignalPathRunner.class)
	
	public SignalPathRunner(List<Map> signalPathData, Globals globals, boolean deleteOnStop = true) {
		this.globals = globals
		this.signalPathService = globals.grailsApplication.mainContext.getBean("signalPathService")
		this.servletContext = globals.grailsApplication.mainContext.getBean("servletContext")
		this.signalPathData = signalPathData
		this.deleteOnStop = deleteOnStop
		
		runnerId = "s-"+new Date().getTime()
		
		if (!servletContext["signalPathRunners"])
			servletContext["signalPathRunners"] = [:]
			
		servletContext["signalPathRunners"].put(runnerId,this)
		
		/**
		 * Instantiate the SignalPaths
		 */
		globals.dataSource = signalPathService.createDataSource(globals.signalPathContext, globals)
		globals.init()

		if (globals.signalPathContext.csv) {
			globals.signalPathContext.speed = 0
		}

		// Instantiate SignalPaths from JSON
		for (int i=0;i<signalPathData.size();i++) {
			SignalPath signalPath = signalPathService.jsonToSignalPath(signalPathData[i],false,globals,true)
			signalPaths.add(signalPath)
		}
	}
	
	public List<SignalPath> getSignalPaths() {
		return signalPaths
	}
	
	public Map getModuleChannelMap(int signalPathIndex) {
		Map result = [:]
		signalPaths[signalPathIndex].modules.each {
			if (it instanceof IHasPushChannel) {
				result.put(it.hash.toString(), it.uiChannelId)
			}
		}
		return result
	}
	
	@Override
	public void run() {
		
		Throwable reportException = null
		setName("SignalPathRunner");
		
		// Run
		try {
			for (SignalPath it : signalPaths)
				it.connectionsReady()
				
			ready = true
			
			if (!signalPaths.isEmpty())
				signalPathService.runSignalPaths(signalPaths)
		} catch (Throwable e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error while running SignalPaths!",e)
			reportException = e
		}

		if (reportException) {
			StringBuilder sb = new StringBuilder(reportException.message)
			while (reportException.cause!=null) {
				reportException = reportException.cause
				sb.append("<br><br>")
				sb.append("Caused by: ")
				sb.append(reportException.message)
			}
			signalPaths.each {SignalPath sp->
				globals?.uiChannel?.push(new ErrorMessage(sb.toString()), sp.uiChannelId)
			}
		}
		
		signalPaths.each {SignalPath sp->
			globals?.uiChannel?.push(new DoneMessage(), sp.uiChannelId)
		}
		
		// Cleanup
		try {
			destroy()
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error while destroying SignalPathRunner!",e)
		}

		log.info("SignalPathRunner is ready.")
	}
	
	/**
	 * Aborts the data feed and releases all resources
	 */
	public void destroy() {
		servletContext["signalPathRunners"].remove(runnerId)
		signalPaths.each {it.destroy()}
		globals.destroy()
		
		if (deleteOnStop)
			signalPathService.deleteRunningSignalPathReferences(this)
		else signalPathService.updateState(getRunnerId(), "stopped")
	}
	
	public void abort() {
		log.info("Aborting SignalPathRunner..")
		globals.abort = true
		globals.dataSource?.stopFeed()
		
		// Will be called in run() before exiting
//		destroy()
		
		// Interrupt whatever this thread is doing
		// Commented out because I witnessed a deadlock leading to this call, revisit later if necessary
		// This thread should exit anyway on the next event, so no big deal if not interrupted
		//this.interrupt()
	}
	
}
