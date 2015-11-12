package com.unifina.signalpath

import com.unifina.push.IHasPushChannel
import com.unifina.service.SignalPathService
import com.unifina.utils.Globals
import grails.util.GrailsUtil
import org.apache.log4j.Logger

/**
 * A Thread that instantiates and runs a list of SignalPaths.
 * Identified by a runnerId, by which this runner can be looked up from the
 * servletContext["signalPathRunners"] map.
 */
public class SignalPathRunner extends Thread {
	private final List<SignalPath> signalPaths = Collections.synchronizedList([])
	private boolean deleteOnStop
	
	String runnerId
	
	Globals globals

	SignalPathService signalPathService
	
	// Have the SignalPaths been instantiated?
	boolean running = false
	
	private static final Logger log = Logger.getLogger(SignalPathRunner.class)

	private List<Runnable> startListeners = []
	private List<Runnable> stopListeners = []

	private SignalPathRunner(Globals globals, boolean deleteOnStop) {
		this.globals = globals
		this.signalPathService = globals.grailsApplication.mainContext.getBean("signalPathService")
		this.deleteOnStop = deleteOnStop

		runnerId = "s-"+new Date().getTime()

		/**
		 * Instantiate the SignalPaths
		 */
		globals.dataSource = signalPathService.createDataSource(globals.signalPathContext, globals)
		globals.init()

		if (globals.signalPathContext.csv) {
			globals.signalPathContext.speed = 0
		}
	}

	public SignalPathRunner(List<Map> signalPathData, Globals globals, boolean deleteOnStop = true) {
		this(globals, deleteOnStop)

		// Instantiate SignalPaths from JSON
		for (int i=0;i<signalPathData.size();i++) {
			SignalPath signalPath = signalPathService.jsonToSignalPath(signalPathData[i],false,globals,true)
			signalPaths.add(signalPath)
		}
	}

	public SignalPathRunner(SignalPath signalPath, Globals globals, boolean deleteOnStop = true) {
		this(globals, deleteOnStop)
		signalPath.globals = globals
		for (AbstractSignalPathModule module : signalPath.getModules()) {
			module.globals = globals
		}
		signalPaths.add(signalPath)
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
	
	public synchronized void setRunning(boolean running) {
		this.running = running
		this.notify()
	}
	
	public synchronized boolean getRunning() {
		return running
	}

	public void addStartListener(Runnable r) {
		startListeners << r
	}

	public void addStopListener(Runnable r) {
		stopListeners << r
	}

	public synchronized void waitRunning(boolean target=true) {
		int i = 0
		while (getRunning() != target && i++<60)
			this.wait(500)
	}
	
	@Override
	public void run() {
		startListeners.each {
			it.run()
		}
		Throwable reportException = null
		setName("SignalPathRunner");
		
		// Run
		try {
			for (SignalPath it : signalPaths)
				it.connectionsReady()
				
			setRunning(true)
			
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
		setRunning(false)
	}
	
	/**
	 * Aborts the data feed and releases all resources
	 */
	public void destroy() {
		stopListeners.each {
			it.run()
		}
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
