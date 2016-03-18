package com.unifina.feed;

import java.io.ByteArrayInputStream;
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
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

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
public abstract class AbstractHistoricalFileFeed<ModuleClass, MessageClass extends ITimestamped, KeyClass, EventRecipientClass extends IEventRecipient> extends AbstractHistoricalFeed<ModuleClass, MessageClass, KeyClass, EventRecipientClass> {
	
	protected HashMap<IEventRecipient,Integer> counts = new HashMap<>();
	protected HashMap<IEventRecipient, FeedFileService.StreamResponse> streams = new HashMap<>();
	protected HashMap<FeedEventIterator, IEventRecipient> recipientByIterator = new HashMap<>();
	
	private static final Logger log = Logger.getLogger(AbstractHistoricalFeed.class);
	
	public AbstractHistoricalFileFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected EventRecipientClass createEventRecipient(ModuleClass subscriber) {
		EventRecipientClass r = super.createEventRecipient(subscriber);
		counts.put(r,0);
		return r;
	}
	
	/**
	 * Extract the Stream required by the specified recipient.
	 * @param recipient
	 * @return
	 */
	protected abstract Stream getStream(IEventRecipient recipient);

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception {
		ArrayList<Feed> feeds = new ArrayList<>(1);
		for (IEventRecipient r : eventRecipients)
			if (!feeds.contains(getStream(r).getFeed()))
				feeds.add(getStream(r).getFeed());
		
		FeedFileService feedFileService = (FeedFileService)globals.getGrailsApplication().getMainContext().getBean("feedFileService");
		
		return feedFileService.getUnits(beginDate, endDate, feeds);
	}

	private FeedEventIterator<MessageClass, EventRecipientClass> createIterator(FeedFile feedFile, Date day, InputStream inputStream, EventRecipientClass recipient) {
		Iterator<MessageClass> contentIterator = createContentIterator(feedFile, day, inputStream, recipient);
		return new FeedEventIterator<>(contentIterator, this, recipient);
	}
	
	/**
	 * Must return an iterator that extracts the actual message content from the specified input stream.
	 * @param day
	 * @param inputStream
	 * @param recipient
	 * @return
	 */
	protected abstract Iterator<MessageClass> createContentIterator(FeedFile feedFile, Date day, InputStream inputStream, EventRecipientClass recipient);
	
	/**
	 * Retrieves a StreamResponse from FileFeedService and creates a FeedEventIterator for the stream.
	 * Streams can be in many pieces, so this should be called for recipients every time their stream
	 * runs out.
	 * @param recipient
	 * @return A FeedEventIterator or null if no more iterators could be created
	 * @throws Exception
	 */
	@Override
	protected FeedEventIterator<MessageClass, EventRecipientClass> getNextIterator(EventRecipientClass recipient) throws IOException {
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
		super.stopFeed();
		
		// Clean state
		for (Entry<IEventRecipient,Integer> entry : counts.entrySet())
			entry.setValue(0);
	}

}
