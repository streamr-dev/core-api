package com.unifina.signalpath

import grails.util.GrailsUtil

import javax.servlet.ServletContext

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.SignalPathService
import com.unifina.security.SecUser
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
		super("SignalPathRunner")

		this.globals = globals
		this.signalPathService = globals.grailsApplication.mainContext.getBean("signalPathService")
		this.servletContext = globals.grailsApplication.mainContext.getBean("servletContext")
		this.signalPathData = signalPathData

		
		runnerId = "s-"+new Date().getTime()
		
		if (!servletContext["signalPathRunners"])
			servletContext["signalPathRunners"] = [:]
			
		servletContext["signalPathRunners"].put(runnerId,this)
		
		signalPathData.eachWithIndex {data, i->
			final String sessionId = runnerId+"-$i"
			final String channel = "/atmosphere/"+sessionId
			
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
		
		Exception reportException = null
		
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
					SignalPath signalPath = signalPathService.jsonToSignalPath(signalPathData[i],false,globals,true)
					signalPath.returnChannel = returnChannels[i]
					returnChannels[i].signalPath = signalPath
					//				signalPath.connectionsReady()
					signalPaths.add(signalPath)
				} catch (Exception e) {
					e = GrailsUtil.deepSanitize(e)
					log.error("Error while instantiating SignalPaths!",e)
					returnChannels[i].sendError(e.toString())
				}
			}

			for (SignalPath it : signalPaths)
				it.connectionsReady()

			ready = true
			signalPathService.runSignalPaths(signalPaths)
		} catch (Exception e) {
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
			returnChannels.each{it.sendError(reportException.message+(reportException.cause!=null ? "<br>Caused by: $reportException.cause" : ""))}
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
		this.interrupt()
	}
	
}
