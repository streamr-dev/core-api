package com.unifina.signalpath

import com.unifina.datasource.IStartListener
import com.unifina.datasource.IStopListener
import com.unifina.domain.signalpath.Canvas
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
	private boolean adhoc

	String runnerId

	Globals globals

	SignalPathService signalPathService

	// Have the SignalPaths been instantiated?
	boolean running = false

	private static final Logger log = Logger.getLogger(SignalPathRunner.class)

	private SignalPathRunner(Globals globals, boolean adhoc) {
		this.globals = globals
		this.signalPathService = globals.grailsApplication.mainContext.getBean("signalPathService")
		this.adhoc = adhoc

		runnerId = "s-" + new Date().getTime()

		/**
		 * Instantiate the SignalPaths
		 */
		globals.dataSource = signalPathService.createDataSource(adhoc, globals)
		globals.realtime = !adhoc
		globals.init()

		if (globals.signalPathContext.csv) {
			globals.signalPathContext.speed = 0
		}
	}

	public SignalPathRunner(List<Map> signalPathMaps, Globals globals, boolean adhoc = true) {
		this(globals, adhoc)

		// Instantiate SignalPaths from JSON
		for (int i = 0; i < signalPathMaps.size(); i++) {
			SignalPath signalPath = signalPathService.mapToSignalPath(signalPathMaps[i], false, globals, true)
			signalPaths.add(signalPath)
		}
	}

	public SignalPathRunner(SignalPath signalPath, Globals globals, boolean adhoc = true) {
		this(globals, adhoc)
		signalPath.setGlobals(globals)
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

	public void addStartListener(IStartListener listener) {
		globals.dataSource.addStartListener(listener)
	}

	public void addStopListener(IStopListener listener) {
		globals.dataSource.addStopListener(listener)
	}

	public synchronized void waitRunning(boolean target = true) {
		int i = 0
		while (getRunning() != target && i++ < 60)
			this.wait(500)
	}

	@Override
	public void run() {
		Throwable reportException = null
		setName("SignalPathRunner");

		try {
			for (SignalPath it : signalPaths) {
				it.connectionsReady()
			}

			globals.dataSource.addStartListener(new IStartListener() {
				@Override
				void onStart() {
					setRunning(true)
				}
			})

			if (!signalPaths.isEmpty()) {
				signalPathService.runSignalPaths(signalPaths)
			}
		} catch (Throwable e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error while running SignalPaths!", e)
			reportException = e
		}

		if (reportException) {
			StringBuilder sb = new StringBuilder(reportException.message)
			while (reportException.cause != null) {
				reportException = reportException.cause
				sb.append("<br><br>")
				sb.append("Caused by: ")
				sb.append(reportException.message)
			}
			signalPaths.each { SignalPath sp ->
				globals?.uiChannel?.push(new ErrorMessage(sb.toString()), sp.uiChannelId)
			}

		}

		signalPaths.each { SignalPath sp ->
			globals?.uiChannel?.push(new DoneMessage(), sp.uiChannelId)
		}

		// Cleanup
		try {
			destroy()
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error while destroying SignalPathRunner!", e)
		}

		log.info("SignalPathRunner is ready.")
		setRunning(false)
	}

	/**
	 * Aborts the data feed and releases all resources
	 */
	public void destroy() {
		signalPaths.each { it.destroy() }
		globals.destroy()

		if (adhoc)
			signalPathService.deleteRunningSignalPathReferences(this)
		else signalPathService.updateState(getRunnerId(), Canvas.State.STOPPED)
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
