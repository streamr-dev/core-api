package com.unifina.signalpath

import grails.util.GrailsUtil

import javax.servlet.ServletContext

import org.apache.log4j.Logger

import com.unifina.service.SignalPathService
import com.unifina.utils.Globals

public class SignalPathRunner extends Thread {
	
	private List<Map> signalPathData
	private final List<SignalPath> signalPaths = Collections.synchronizedList([])
	private final List<SignalPathReturnChannel> returnChannels = Collections.synchronizedList([])
	
	String runnerId
	
	Globals globals
	
	ServletContext servletContext
	SignalPathService signalPathService
	
	// Have the SignalPaths been instantiated?
	boolean ready = false
	
	private static final Logger log = Logger.getLogger(SignalPathRunner.class)
	
	public SignalPathRunner(List<Map> signalPathData, Globals globals) {
		this.globals = globals
		this.signalPathService = globals.grailsApplication.mainContext.getBean("signalPathService")
		this.servletContext = globals.grailsApplication.mainContext.getBean("servletContext")
		this.signalPathData = signalPathData
//		setContextClassLoader(globals.classLoader)
		
		runnerId = "s-"+new Date().getTime()
		
		if (!servletContext["signalPathRunners"])
			servletContext["signalPathRunners"] = [:]
			
		servletContext["signalPathRunners"].put(runnerId,this)
		
		signalPathData.eachWithIndex {data, i->
			final String sessionId = runnerId+"-$i"
			final String channel = sessionId
			
			SignalPathReturnChannel returnChannel = new SignalPathReturnChannel(sessionId, channel, servletContext)
			returnChannels << returnChannel
		}
	}
	
	public SignalPathReturnChannel getReturnChannel(int index) {
		return returnChannels[index]
	}
	
	public SignalPathReturnChannel getReturnChannel(String sessionId) {
		return returnChannels.find {it.sessionId==sessionId}
	}
	
	@Override
	public void run() {
		
		Throwable reportException = null
		setName("SignalPathRunner");
		
		// Run
		try {
			globals.dataSource = signalPathService.createDataSource(globals.signalPathContext, globals)
			globals.init()

			if (globals.signalPathContext.csv) {
//				globals.signalPathContext.csvOptions = [timeFormat:Integer.parseInt(globals.signalPathContext.csvOptions.csvTimeFormat), filterEmpty:(globals.signalPathContext.csvOptions.filterEmpty ? true:false)]
				globals.signalPathContext.speed = 0
			}

			// Instantiate SignalPaths from JSON
			for (int i=0;i<signalPathData.size();i++) {
				try {
					SignalPath signalPath = signalPathService.jsonToSignalPath(signalPathData[i],false,globals,returnChannels[i],true)
//					signalPath.returnChannel = returnChannels[i] // set in SignalPath constructor
//					returnChannels[i].signalPath = signalPath // set in SignalPath constructor
					//				signalPath.connectionsReady()
					signalPaths.add(signalPath)
				} catch (Exception e) {
					e = GrailsUtil.deepSanitize(e)
					log.error("Error while instantiating SignalPaths!",e)
					returnChannels[i].sendError(e.getMessage() ?: e.toString())
				}
			}

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

		// Cleanup
		try {
			destroy()
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error while destroying SignalPathRunner!",e)
		}

		if (reportException) {
			StringBuilder sb = new StringBuilder(reportException.message)
			while (reportException.cause!=null) {
				reportException = reportException.cause
				sb.append("<br><br>")
				sb.append("Caused by: ")
				sb.append(reportException.message)
			}
			returnChannels.each{it.sendError(sb.toString())}
		}
		
		returnChannels.each {it.sendDone()}

		log.info("SignalPathRunner is ready.")
	}
	
	/**
	 * Aborts the data feed and releases all resources
	 */
	public void destroy() {
		// Can't call abort, because the returnChannels might have pending messages
//		abort()
		servletContext["signalPathRunners"].remove(runnerId)
		signalPaths.each {it.destroy()}
		globals.destroy()
	}
	
	public void abort() {
		log.info("Aborting SignalPathRunner..")
		globals.abort = true
		globals.dataSource?.stopFeed()
		returnChannels.each {it.destroy()}
		destroy()
		
		// Interrupt whatever this thread is doing
		// Commented out because I witnessed a deadlock leading to this call, revisit later if necessary
		// This thread should exit anyway on the next event, so no big deal if not interrupted
		//this.interrupt()
	}
	
}
