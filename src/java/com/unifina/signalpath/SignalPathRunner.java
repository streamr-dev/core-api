package com.unifina.signalpath;

import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.service.SignalPathService;
import com.unifina.utils.Globals;
import com.unifina.utils.IdGenerator;
import grails.util.GrailsUtil;
import grails.util.Holders;
import org.apache.log4j.Logger;

/**
 * A Thread that instantiates and runs a SignalPath.
 * Identified by a runnerId, by which this runner can be looked up from signalPathService.
 */
public class SignalPathRunner extends Thread {
	private static final Logger log = Logger.getLogger(SignalPathRunner.class);

	private final String runnerId;
	private final Globals globals;
	private final SignalPath signalPath;
	private boolean running = false;
	private Throwable thrownOnStartUp;

	public SignalPathRunner(SignalPath signalPath, Globals globals) {
		this.runnerId = IdGenerator.get();
		this.globals = globals;
		this.signalPath = signalPath;

		signalPath.setGlobals(globals);
		setName(buildThreadName(signalPath));
	}

	public String getRunnerId() {
		return runnerId;
	}

	public Globals getGlobals() {
		return globals;
	}

	public SignalPath getSignalPath() {
		return signalPath;
	}

	public synchronized boolean getRunning() {
		return running;
	}

	public Throwable getThrownOnStartUp() {
		return thrownOnStartUp;
	}

	public synchronized void waitRunning(boolean target) throws InterruptedException {
		int i = 0;
		while (getRunning() != target && i++ < 60) {
			log.debug(String.format("Waiting for %s to %s...", runnerId, target ? "start" : "stop"));
			if (target && thrownOnStartUp != null) {
				log.error("Giving up on waiting because run threw exception.");
				break;
			} else {
				wait(500);
			}
		}

		if (getRunning() != target) {
			log.error("Timed out while waiting for runner to " + (target ? "start" : "stop"));
		}
	}

	public void addStartListener(IStartListener listener) {
		globals.getDataSource().addStartListener(listener);
	}

	public void addStopListener(IStopListener listener) {
		globals.getDataSource().addStopListener(listener);
	}

	@Override
	public void run() {
		try {
			signalPath.connectionsReady();
			addStartListener(() -> setRunning(true));

			// Start feed, blocks until event loop exists
			globals.getDataSource().start();
		} catch (Throwable e) {
			final Throwable sanitized = GrailsUtil.deepSanitize(e);
			thrownOnStartUp = sanitized;
			log.error("Error while running SignalPaths", e);
			safeRun(() -> pushErrorToUiChannels(sanitized, signalPath));
		}

		safeRun(() -> signalPath.pushToUiChannel(new DoneMessage()));

		if (getGlobals().isAdhoc()) {
			safeRun(() -> signalPath.pushToUiChannel(new ByeMessage()));
		}

		// Cleanup
		try {
			destroyMe();
		} catch (Throwable e) {
			e = GrailsUtil.deepSanitize(e);
			log.error("Error while destroying SignalPathRunner", e);
		}

		log.info("SignalPathRunner is done.");
		setRunning(false);
	}

	public void abort() {
		log.info("Aborting...");
		globals.getDataSource().abort();
	}

	private synchronized void setRunning(boolean running) {
		this.running = running;
		log.debug("setRunning (" + runnerId + ": " + running);
		notify();
	}

	/**
	 * Aborts the data feed and releases all resources
	 */
	private void destroyMe() {
		SignalPathService signalPathService = Holders.getApplicationContext().getBean(SignalPathService.class);
		safeRun(signalPath::destroy);

		if (globals.isAdhoc()) {
			// Mark only adhoc canvases to stopped state
			safeRun(() -> signalPathService.updateState(runnerId, Canvas.State.STOPPED));
			// Delayed-delete the references to allow UI to catch up
			safeRun(() -> signalPathService.deleteReferences(signalPath, true));
		}

		safeRun(globals::destroy);
	}

	private void safeRun(Runnable r) {
		try {
			r.run();
		} catch (Exception e) {
			log.error(e);
		}
	}

	private static String buildThreadName(SignalPath signalPath) {
		StringBuilder nameBuilder = new StringBuilder("SignalPathRunner [");
		Canvas canvas = signalPath.getCanvas();
		String canvasId = canvas != null ? canvas.getId() : "(canvas is null)";
		nameBuilder.append(canvasId);
		nameBuilder.append("]");
		return nameBuilder.toString();
	}


	private static void pushErrorToUiChannels(Throwable e, SignalPath signalPath) {
		StringBuilder sb = new StringBuilder(e.getMessage());
		while (e.getCause() != null) {
			e = e.getCause();
			sb.append("<br><br>");
			sb.append("Caused by: ");
			sb.append(e.getMessage());
		}

		signalPath.pushToUiChannel(new ErrorMessage(sb.toString()));
	}
}
