package com.unifina.datasource;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.data.Event;
import com.unifina.data.EventQueueMetrics;
import com.unifina.feed.MessageRouter;
import com.unifina.feed.StreamPropagationRoot;
import com.unifina.feed.StreamMessageSource;
import com.unifina.serialization.SerializationRequest;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.SignalPath;
import com.unifina.signalpath.StopRequest;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * DataSource wires together SignalPaths and streams of messages and other Events.
 * There are two types: HistoricalDataSource and RealtimeDataSource.
 *
 * The DataSource is started with DataSource#start(), which starts the event loop
 * and blocks until done.
 */
public abstract class DataSource {
	private static final Logger log = Logger.getLogger(DataSource.class);

	private final Set<SignalPath> signalPaths = new HashSet<>();
	private final List<IStartListener> startListeners = new ArrayList<>();
	private final List<IStopListener> stopListeners = new ArrayList<>();
	private boolean running = false;

	// DataSource and Globals always have a 1-to-1 relationship.
	protected final Globals globals;

	private final Map<StreamPartition, StreamPropagationRoot> eventRecipientByStreamPartition = new HashMap<>();
	private final MessageRouter router = new MessageRouter();

	private DataSourceEventQueue eventQueue;

	public DataSource(Globals globals) {
		this.globals = globals;
		this.eventQueue = createEventQueue();
	}

	/**
	 * Adds an event to the event queue.
	 */
	public void enqueue(Event event) {
		eventQueue.enqueue(event);
	}

	/**
	 * Connects a SignalPath to this DataSource. This has the effect of subscribing the
	 * DataSource to all the streams required by the Modules in the SignalPath, as well
	 * as wiring time listeners to the clock.
	 */
	public void connectSignalPath(SignalPath sp) {
		signalPaths.add(sp);
		for (AbstractSignalPathModule it : sp.getModules()) {
			register(it);
		}
	}

	/**
	 * Registers an object with this DataSource. The types that have some effect are:
	 * AbstractStreamSourceModules, ITimeListeners, and IDayListeners. It's safe to call this method
	 * with other types of arguments, they will be ignored.
	 */
	private void register(Object o) {
		if (o instanceof ConfigurableStreamModule) {
			subscribe((ConfigurableStreamModule) o);
		}
		if (o instanceof ITimeListener) {
			eventQueue.addTimeListener((ITimeListener) o);
		}
		if (o instanceof IDayListener) {
			eventQueue.addDayListener((IDayListener) o);
		}
	}

	/**
	 * Adds an IStartListener to this DataSource. Its onStart() method will be called just before
	 * starting the data flow. If the DataSource is already started, the listener will be called
	 * immediately.
	 */
	public void addStartListener(IStartListener startListener) {
		startListeners.add(startListener);
		if (running) {
			startListener.onStart();
		}
	}

	public void addStopListener(IStopListener stopListener) {
		stopListeners.add(stopListener);
	}

	/**
	 * Starts the main event loop and blocks until it is stopped.
	 * This should be called from the event processing Thread.
	 */
	public void start() {
		for (IStartListener startListener : startListeners) {
			startListener.onStart();
		}

		final StreamMessageSource streamMessageSource;
		try {
			streamMessageSource = createStreamMessageSource(
				eventRecipientByStreamPartition.keySet(), // All the subscribed StreamPartitions
				new StreamMessageSource.StreamMessageConsumer() {
					@Override
					public void accept(StreamMessage streamMessage) {
						// Consult the router to find consumers who need to receive this StreamMessage
						router.route(streamMessage)
							.forEach(routedConsumer ->
								// Enqueue an event for each consumer registered with the router
								DataSource.this.enqueue(new Event<>(
									streamMessage,
									streamMessage.getTimestampAsDate(),
									streamMessage.getSequenceNumber(),
									routedConsumer
								)));
					}

					@Override
					public void done() {
						// Enqueue an end event, which when processed, aborts the event queue.
						DataSource.this.enqueue(new Event<>(
							null,
							globals.getEndDate() != null ? globals.getEndDate() : new Date(),
							(nul) -> abort()
						));
					}
				});
		} catch (Exception e) {
			throw new RuntimeException("Error while creating StreamMessageSource", e);
		}

		try {
			// Enqueue a start event to set the start time
			enqueue(new Event<>(
				null,
				globals.getStartDate() != null ? globals.getStartDate() : new Date(),
				(nul) -> {
					running = true;
					log.info("Event queue running.");
				}
			));
			eventQueue.start();
		} catch (Exception e) {
			throw new RuntimeException("Error while processing event queue", e);
		} finally {
			running = false;

			// Final serialization requests
			for (SignalPath signalPath : getSerializableSignalPaths()) {
				SerializationRequest.makeFeedEvent(signalPath).dispatch();
			}

			for (IStopListener it : stopListeners) {
				try {
					it.onStop();
				} catch (Exception e) {
					log.error("Exception thrown while stopping feed", e);
				}
			}

			if (streamMessageSource != null) {
				try {
					streamMessageSource.close();
				} catch (Exception e) {
					log.error("Exception thrown while closing StreamMessageSource (ignored)", e);
				}
			}
		}

		log.info("DataSource has stopped. " + retrieveMetricsAndReset());
	}

	public void abort() {
		// Add stop request to queue
		Date stopTime = globals.getTime() != null ? globals.getTime() : new Date();
		enqueue(new Event<>(new StopRequest(stopTime), stopTime, 0L, (stopRequest) -> {
			try {
				eventQueue.abort();
			} catch (Exception e) {
				log.error("Exception thrown while aborting eventQueue (ignored)", e);
			}
		}));
	}

	public EventQueueMetrics retrieveMetricsAndReset() {
		return eventQueue.retrieveMetricsAndReset();
	}

	protected abstract StreamMessageSource createStreamMessageSource(Collection<StreamPartition> streamPartitions, StreamMessageSource.StreamMessageConsumer consumer);

	protected DataSourceEventQueue createEventQueue() {
		return new DataSourceEventQueue(globals, this);
	}

	protected Iterable<SignalPath> getSerializableSignalPaths() {
		List<SignalPath> serializableSps = new ArrayList<>();
		for (SignalPath sp : signalPaths) {
			if (sp.isSerializable()) {
				serializableSps.add(sp);
			}
		}
		return serializableSps;
	}

	/**
	 * For each subscribed StreamPartition, there is a StreamPropagationRoot which contains the
	 * set of modules subscribed to that StreamPartition and acts as a propagation root.
	 */
	private void subscribe(ConfigurableStreamModule module) {
		Collection<StreamPartition> streamPartitions = module.getStreamPartitions();

		for (StreamPartition sp : streamPartitions) {

			// Create and register the event recipient for this StreamPartition if it doesn't already exist
			StreamPropagationRoot recipient = eventRecipientByStreamPartition.get(sp);
			if (recipient == null) {
				recipient = new StreamPropagationRoot(this);
				eventRecipientByStreamPartition.put(sp, recipient);
				router.subscribe(recipient, sp);
			}

			recipient.register(module);
		}
	}

}
