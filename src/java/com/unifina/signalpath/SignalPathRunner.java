package com.unifina.signalpath;

import com.unifina.datasource.*;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.service.SignalPathService;
import com.unifina.utils.Globals;
import com.unifina.utils.IdGenerator;
import grails.util.GrailsUtil;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A Thread that instantiates and runs a list of SignalPaths.
 * Identified by a runnerId, by which this runner can be looked up from the
 * servletContext["signalPathRunners"] map.
 */
public class SignalPathRunner extends Thread {
	private static final Logger log = Logger.getLogger(SignalPathRunner.class);

	private final String runnerId;
	private final Globals globals;
	private final List<SignalPath> signalPaths;
	private boolean running = false;
	private Throwable thrownOnStartUp;

	public SignalPathRunner(SignalPath signalPath, Globals globals, boolean adhoc) {
		this(Collections.singleton(signalPath), globals, adhoc);
	}

	private SignalPathRunner(Collection<SignalPath> signalPaths, Globals globals, boolean adhoc) {
		this.runnerId = IdGenerator.get();
		this.globals = globals;

		globals.setDataSource(createDataSource(adhoc, globals));
		globals.setRealtime(!adhoc);
		globals.init();
		for (SignalPath sp : signalPaths) {
			sp.setGlobals(globals);
		}
		this.signalPaths = Collections.unmodifiableList(new ArrayList<>(signalPaths));
		setName(buildThreadName(this.signalPaths));
	}

	public String getRunnerId() {
		return runnerId;
	}

	public Globals getGlobals() {
		return globals;
	}

	public List<SignalPath> getSignalPaths() {
		return signalPaths;
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
			log.debug("Waiting for " + runnerId + " to start...");
			if (target && thrownOnStartUp != null) {
				log.info("Giving up on waiting because run threw exception.");
			} else {
				wait(500);
			}
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
			for (SignalPath it : signalPaths) {
				it.connectionsReady();
			}

			addStartListener(new IStartListener() {
				@Override
				public void onStart() {
					setRunning(true);
				}
			});

			if (!signalPaths.isEmpty()) {
				runSignalPaths();
			}
		} catch (Throwable e) {
			thrownOnStartUp = e = GrailsUtil.deepSanitize(e);
			log.error("Error while running SignalPaths", e);
			pushErrorToUiChannels(e, signalPaths);
		}

		for (SignalPath sp : signalPaths) {
			sp.pushToUiChannel(new DoneMessage());
			if (getGlobals().isAdhoc()) {
				sp.pushToUiChannel(new ByeMessage());
			}
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
		globals.getDataSource().stopFeed();
	}

	private synchronized void setRunning(boolean running) {
		this.running = running;
		log.debug("setRunning (" + runnerId + ": " + running);
		notify();
	}

	private void runSignalPaths() {
		// Start feed, blocks until feed is complete
		try {
			globals.getDataSource().startFeed();
		} finally {
			// Stop the feed, cleanup
			globals.getDataSource().stopFeed();
		}
	}

	/**
	 * Aborts the data feed and releases all resources
	 */
	private void destroyMe() {
		for (SignalPath signalPath : signalPaths) {
			signalPath.destroy();
		}
		SignalPathService signalPathService = Holders.getApplicationContext().getBean(SignalPathService.class);
		signalPathService.updateState(runnerId, Canvas.State.STOPPED);

		if (globals.isAdhoc()) {
			for (SignalPath sp : getSignalPaths()) {
				// Delayed-delete the references to allow UI to catch up
				signalPathService.deleteReferences(sp, true);
			}
		}
	}

	private static DataSource createDataSource(boolean adhoc, Globals globals) {
		if (adhoc) {
			return new HistoricalDataSource(globals);
		} else {
			return new RealtimeDataSource(globals);
		}
	}

	private static String buildThreadName(List<SignalPath> signalPaths) {
		StringBuilder nameBuilder = new StringBuilder("SignalPathRunner [");
		for (int i=0; i < signalPaths.size(); i++) {
			if (i > 0) {
				nameBuilder.append(", ");
			}
			Canvas canvas = signalPaths.get(i).getCanvas();
			String canvasId = canvas != null ? canvas.getId() : "(canvas is null)";
			nameBuilder.append(canvasId);
		}
		nameBuilder.append("]");
		return nameBuilder.toString();
	}


	private static void pushErrorToUiChannels(Throwable e, List<SignalPath> signalPaths) {
		StringBuilder sb = new StringBuilder(e.getMessage());
		while (e.getCause() != null) {
			e = e.getCause();
			sb.append("<br><br>");
			sb.append("Caused by: ");
			sb.append(e.getMessage());
		}

		for (SignalPath sp : signalPaths) {
			sp.pushToUiChannel(new ErrorMessage(sb.toString()));
		}
	}
}
