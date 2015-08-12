package com.unifina.feed;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.service.FeedFileService;
import com.unifina.utils.Globals;

/**
 * This class implements a basic historical feed that reads events from
 * files it obtains via DataService.
 * @author Henri
 */
public abstract class AbstractHistoricalFileFeed extends AbstractHistoricalFeed {

	protected ArrayList<IEventRecipient> eventRecipients = new ArrayList<>();
	protected boolean started = false;
	
	protected HashMap<IEventRecipient,Integer> counts = new HashMap<>();
	protected HashMap<IEventRecipient, FeedFileService.StreamResponse> streams = new HashMap<>();
	protected HashMap<FeedEventIterator, IEventRecipient> recipientByIterator = new HashMap<>();
	
	protected PriorityQueue<FeedEvent> queue = new PriorityQueue<>();
	
	private static final Logger log = Logger.getLogger(AbstractHistoricalFeed.class);
	
	public AbstractHistoricalFileFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected IEventRecipient createEventRecipient(Object subscriber) {
		IEventRecipient r = super.createEventRecipient(subscriber);
//		doCreateEventRecipient(subscriber);
		eventRecipients.add(r);
		counts.put(r,0);
		return r;
	}
	
	/**
	 * Creates an IEventRecipient for FeedEvents intended for the specified subscriber.
	 * @param subscriber
	 * @return
	 */
//	protected abstract IEventRecipient doCreateEventRecipient(Object subscriber);
	
	/**
	 * Extracts a Date from the specified event content.
	 * @param eventContent the content return by the content iterator, for which a timestamp is needed
	 * @param contentIterator the contentIterator that produced the eventContent
	 * @return
	 */
	protected abstract Date getTimestamp(Object eventContent, Iterator<Object> contentIterator);
	
	/**
	 * Extract the Stream required by the specified recipient.
	 * @param recipient
	 * @return
	 */
	protected abstract Stream getStream(IEventRecipient recipient);
	
	@Override
	public FeedEvent getNext() throws Exception {
		FeedEvent event = queue.poll();
		
		if (event==null)
			return null;
		
		// From the same stream, get the next event
		FeedEventIterator iterator = (FeedEventIterator) event.iterator; // TODO: avoid cast?
		FeedEvent nxt = iterator.next();
		
		// No next event, try to get the next stream piece and the next event from there
		while (nxt==null && started) {
			// Close the old feed reader
			if (iterator instanceof Closeable)
				((Closeable)iterator).close();
			
			iterator = getNextIterator(iterator.getRecipient());
			// If the next stream was found, try to get an event
			if (iterator!=null)
				nxt = iterator.next();
			// If no next stream, the we're done for this orderbook
			else break;
		}
		
		// If the next event exists, add it to the queue
		if (nxt!=null) {
			queue.add(nxt);
		}
		
		return event;
	}

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception {
		ArrayList<Feed> feeds = new ArrayList<>(1);
		for (IEventRecipient r : eventRecipients)
			if (!feeds.contains(getStream(r).getFeed()))
				feeds.add(getStream(r).getFeed());
		
		FeedFileService feedFileService = (FeedFileService)globals.getGrailsApplication().getMainContext().getBean("feedFileService");
		
//		if (backtest.getDateSet()==null) {
		return feedFileService.getUnits(beginDate, endDate, feeds);
//		}
//		else {
//			ArrayList<Date[]> result = new ArrayList<>();
//			for (DateRange dr : backtest.getDateSet().getDates()) {
//				result.addAll(feedFileService.getUnits(dr.getBeginDay(), dr.getEndDay(), feeds));
//			}
//			return result;
//		}
	}

	@Override
	public void startFeed() throws Exception {
		started = true;
		
		log.debug("Starting feed with event recipients by key: "+eventRecipientsByKey);
		log.debug("Starting feed with streams: "+streams);

		// For each recipient get an input stream and place the first event in a PriorityQueue
		for (IEventRecipient recipient : eventRecipients) {
			FeedEventIterator iterator = getNextIterator(recipient);
			if (iterator!=null) {
				FeedEvent event = iterator.next();
				if (event!=null)
					queue.add(event);
			}
		}
		
		log.debug("Starting contents of event queue: "+queue);
	}

	private FeedEventIterator createIterator(FeedFile feedFile, Date day, InputStream inputStream, IEventRecipient recipient) {
		Iterator<Object> contentIterator = createContentIterator(feedFile, day, inputStream, recipient);
		return new FeedEventIterator(contentIterator, this, recipient);
	}
	
	/**
	 * Must return an iterator that extracts the actual message content from the specified input stream.
	 * @param day
	 * @param inputStream
	 * @param recipient
	 * @return
	 */
	protected abstract Iterator<Object> createContentIterator(FeedFile feedFile, Date day, InputStream inputStream, IEventRecipient recipient);
	
	/**
	 * Retrieves a StreamResponse from FileFeedService and creates a FeedEventIterator for the stream.
	 * Streams can be in many pieces, so this should be called for recipients every time their stream
	 * runs out.
	 * @param recipient
	 * @return
	 * @throws Exception
	 */
	private FeedEventIterator getNextIterator(IEventRecipient recipient) throws Exception {
		while (true) {
			Integer cnt = counts.get(recipient);
			
			FeedFileService.StreamResponse response = streams.get(recipient);
			
			if (response!=null && response.getStream()!=null)
				response.getInputStream().close();
			
			response = ((FeedFileService)globals.getGrailsApplication().getMainContext().getBean("feedFileService")).getStream(getStream(recipient), globals.getStartDate(), globals.getEndDate(), cnt);
			
			streams.put(recipient, response);
			
			// Null signals end of processing
			if (response==null) {
				log.debug("getNextIterator: no more iterators for recipient "+recipient);
				return null;
			}
			else {
				counts.put(recipient, cnt+1);
				if (response.getSuccess() && (response.getFileSize()==null || response.getFileSize()>0)) {
//					BufferedReader reader;
					InputStream inputStream;
					
				    // If not streaming from local file or if the file is large, don't memory map the file
				    if (!response.getIsFile() || response.getFileSize()>10*1024*1024) {
				    	int bufferBytes = (response.getFileSize()!=null ? Math.min(response.getFileSize().intValue(), 8196) : 8196);
				    	inputStream = response.getInputStream();
				    }
				    // Memory map all smaller files
				    else {
					    FileChannel fc = ((FileInputStream)response.getInputStream()).getChannel();

					    MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
					    
					    byte[] buffer = new byte[(int)fc.size()];
					    mmb.get(buffer);

					    inputStream = new ByteArrayInputStream(buffer);				    	
				    }
				    
					// Decompress on the fly if the stream is compressed
					if (response.getIsCompressed())
						inputStream = new GZIPInputStream(inputStream);
					
					// Set the final InputStream back to the response object so that the correct stream will be closed
					response.setInputStream(inputStream);
					return createIterator(response.getFeedFile(), response.getDay(), inputStream, recipient);
				}
				else {
					// Counter has been incremented, so loop to next piece
				}
			}
		}
	}
	
	@Override
	public void stopFeed() throws Exception {
		started = false;
		
		// Close all the remaining unclosed sources
		for (FeedEvent event : queue) {
			if (event.iterator instanceof Closeable) {
				((Closeable)event.iterator).close();
			}
		}
		
		// Clean state
		for (Entry<IEventRecipient,Integer> entry : counts.entrySet())
			entry.setValue(0);
	}

	/**
	 * A helper class to apply a static recipient and iterator to FeedEvents,
	 * whose content is pulled from a separate content iterator.
	 * @author Henri
	 */
	class FeedEventIterator implements Iterator<FeedEvent> {

		private Iterator<Object> contentIterator;
		private IEventRecipient recipient;
		private AbstractHistoricalFileFeed feed;
		
		private final Logger log = Logger.getLogger(FeedEventIterator.class);
		
		public FeedEventIterator(Iterator<Object> contentIterator, AbstractHistoricalFileFeed feed, IEventRecipient recipient) {
			this.contentIterator = contentIterator;
			this.recipient = recipient;
			this.feed = feed;
		}
		
		@Override
		public boolean hasNext() {
			return contentIterator.hasNext();
		}

		@Override
		public FeedEvent next() {
			Object content = contentIterator.next();
			if (content==null)
				return null;
			
			FeedEvent fe = new FeedEvent(content, feed.getTimestamp(content, contentIterator), recipient);
			fe.feed = feed;
			fe.iterator = this;
			return fe;
		}

		@Override
		public void remove() {
			throw new RuntimeException("Remove operation is not supported!");
		}
		
		public void close() {
			if (contentIterator instanceof Closeable)
				try {
					((Closeable)contentIterator).close();
				} catch (IOException e) {
					log.error("Failed to close content iterator: "+contentIterator);
				}
		}

		public IEventRecipient getRecipient() {
			return recipient;
		}
	}
	
}
