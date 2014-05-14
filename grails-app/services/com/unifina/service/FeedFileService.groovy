package com.unifina.service

import grails.converters.JSON
import groovy.transform.CompileStatic

import java.nio.channels.Channel
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractFeedPreprocessor
import com.unifina.feed.file.AbstractFeedFileDiscoveryUtil
import com.unifina.feed.file.FileStorageAdapter
import com.unifina.feed.file.RemoteFeedFile
import com.unifina.task.FeedFileCreateAndPreprocessTask
import com.unifina.task.FeedFilePreprocessTask
import com.unifina.utils.TimeOfDayUtil

class FeedFileService {

	GrailsApplication grailsApplication
	
	private static final Logger log = Logger.getLogger(FeedFileService.class)
	
	TaskService taskService
	
//    boolean checkAccess(SecUser user,String dataToken,Date day,OrderBookDirectory ob) {
//		/**
//		 * Check that the data tokens match
//		 */
//		if (dataToken==null || !dataToken.equals(user.dataToken))
//			return false
//		
//		/**
//		 * Check that the user has a proper MarketSubscription
//		 */
//		if (user.subscriptions.find {it.market == ob.market && it.depth == ob.depth && useCase.type=="Historical"}==null)
//			return false
//		
//		return true
//    }
	
	FeedFile getFeedFile(Long id) {
		FeedFile.get(id)
	}
	
	FeedFile getFeedFile(Feed feed, Date beginDate, Date endDate, String name) {
		return FeedFile.withCriteria(uniqueResult:true) {
			eq("feed",feed)
			eq("beginDate",beginDate)
			eq("endDate",endDate)
			eq("name",name)
		}
	}
	
	FeedFile getFeedFile(RemoteFeedFile file) {
		return getFeedFile(file.feed, file.beginDate, file.endDate, file.name)
	}

	public void setPreprocessed(FeedFile feedFile) {
		feedFile = feedFile.merge()
		feedFile.processed = true
		feedFile.processing = false
		feedFile.save(failOnError:true)
	}
	
	/**
	 * Creates and saves a task to preprocess the given FeedFile
	 * (if it is not yet processed).
	 * @param feedFile
	 * @return
	 */
	public createPreprocessTask(FeedFile feedFile) {
		if (!feedFile.processed) {
			Task task = new Task(FeedFilePreprocessTask.class.getName(), (FeedFilePreprocessTask.getConfig(feedFile) as JSON).toString(), "preprocess", taskService.createTaskGroupId())
			task.save(flush:true, failOnError:true)
			
			feedFile.processTaskCreated = true
			feedFile.save(flush:true, failOnError:true)
			return task
		}
		else return null
	}
	
	/**
	 * Creates and saves a task to preprocess the given FeedFile
	 * (if it is not yet processed).
	 * @param file
	 * @return
	 */
	public createCreateAndPreprocessTask(RemoteFeedFile file) {
		if (!getFeedFile(file)) {
			Task task = new Task(FeedFileCreateAndPreprocessTask.class.getName(), (file.getConfig() as JSON).toString(), "preprocess", taskService.createTaskGroupId())
			task.save(flush:true, failOnError:true)
			return task
		}
		else return null
	}
	
	void preprocess(FeedFile file) {
		AbstractFeedPreprocessor preprocessor = getPreprocessor(file.feed)
		preprocessor.preprocess(file)
		// Check for new Streams
		List<Stream> streams = preprocessor.getFoundStreams()
		streams.each {Stream s->
			if (!Stream.findByFeedAndLocalId(file.feed, s.localId))
				s.save(failOnError:true)
		}
	}
	
	AbstractFeedPreprocessor getPreprocessor(Feed feed) {
		if (feed.getPreprocessor())
			return this.getClass().getClassLoader().loadClass(feed.getPreprocessor()).newInstance()
		else return null
	}
	
	AbstractFeedFileDiscoveryUtil getDiscoveryUtil(Feed feed) {
		if (feed.discoveryUtilClass)
			return this.getClass().getClassLoader().loadClass(feed.discoveryUtilClass).newInstance(grailsApplication, feed, (feed.discoveryUtilConfig ? JSON.parse(feed.discoveryUtilConfig) : [:]))
		else return null
	}
	
	private String makeCacheFileName(Feed feed, Date day, String filename, boolean compressed) {
		filename = getCompressedName(filename, compressed)
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd")
		String cacheDir = grailsApplication.config.unifina.feed.cachedir
		String separator = System.getProperty("file.separator")
		return "${cacheDir}${separator}${feed.directory}${separator}${df.format(day)}${separator}${filename}"
	}
	
	private String getCanonicalName(Feed feed, Date day, String filename) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd")
		return "${feed.directory}/${df.format(day)}/${filename}"
	}
	
	@CompileStatic
	private static String getCompressedName(String filename, boolean compressed) {
		// Append .gz to filename if requested compressed
		if (compressed)
			filename = (filename.endsWith(".gz") ? filename : filename+".gz")
		else filename = filename.replace(".gz", "")
	}
	
	@CompileStatic
	private InputStream getInputStream(Feed feed, Date day, String filename, boolean compressed) {
		filename = getCompressedName(filename, compressed)
		String canonicalName = getCanonicalName(feed, day, filename)
		return getFileStorageAdapter().retrieve(canonicalName)
	}
	
	public FileStorageAdapter getFileStorageAdapter() {
		if (!grailsApplication.config.unifina.feed.fileStorageAdapter)
			throw new RuntimeException("File storage adapter is not configured!")
			
		return this.getClass().getClassLoader().loadClass(grailsApplication.config.unifina.feed.fileStorageAdapter).newInstance(grailsApplication.config)
	}
	
	StreamResponse getFeed(FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		// First try compressed
		InputStream is = getInputStream(feedFile.feed, feedFile.day, feedFile.name, true)
		if (is!=null)
			return new StreamResponse(inputStream:is, feed:feedFile.feed, day:feedFile.getDay(), success:true, isFile:false, isCompressed:true)

		is = getInputStream(feedFile.feed, feedFile.day, feedFile.name, false)
		if (is!=null)
			return new StreamResponse(inputStream:is, feed:feedFile.feed, day:feedFile.getDay(), success:true, isFile:false, isCompressed:false)
			
		return new StreamResponse(success:false)
	}
	
	StreamResponse getStream(Stream stream, Date beginDate, Date endDate, int piece) {
		// Which feed?
		Feed feed = stream.getFeed()
		
		// Which feed file?
		FeedFile feedFile = FeedFile.findByFeedAndDayBetween(feed, beginDate, endDate, [sort:'day', max:1, offset:piece])
		
		// Null signals the end of data
		if (feedFile==null)
			return null
		
		// Instantiate preprocessor and get the preprocessed file name
		AbstractFeedPreprocessor preprocessor = getPreprocessor(feed)
		
		// streamFileName is the uncompressed name (without .gz suffix)
		String streamFileName = (preprocessor ? preprocessor.getPreprocessedFileName(feedFile.getName(), stream, false) : feedFile.getName())
		
		Boolean useCache = grailsApplication.config.unifina.feed.useCache
		
		if (!useCache) {
			// First try compressed version
			InputStream is = getInputStream(feed, feedFile.day, streamFileName, true)
			if (is!=null)
				return new StreamResponse(inputStream:is, stream:stream, feed:feed, day:feedFile.getDay(), success:true, isFile:false, isCompressed:true)
			
			is = getInputStream(feed, feedFile.day, streamFileName, false)
			if (is!=null)
				return new StreamResponse(inputStream:is, stream:stream, feed:feed, day:feedFile.getDay(), success:true, isFile:false, isCompressed:false)
		
			return new StreamResponse(success:false)
		}
		else {
			// Exists in cache compressed?
			File cachedFile = new File(makeCacheFileName(feed, feedFile.day, streamFileName, true))
			if (cachedFile.canRead()) {
				return new StreamResponse(inputStream:new FileInputStream(cachedFile), stream:stream, feed:feed, day:feedFile.getDay(), success:true, fileSize: cachedFile.length(), isFile:true, isCompressed:true)
			}
			// Exists in cache uncompressed?
			cachedFile = new File(makeCacheFileName(feed, feedFile.day, streamFileName, false))
			if (cachedFile.canRead()) {
				return new StreamResponse(inputStream:new FileInputStream(cachedFile), stream:stream, feed:feed, day:feedFile.getDay(), success:true, fileSize: cachedFile.length(), isFile:true, isCompressed:false)
			}			

			// Try to get compressed from server
			boolean compressed = true
			InputStream is = getInputStream(feed, feedFile.day, streamFileName, compressed)
			if (is==null) {
				// Try again uncompressed
				compressed = false
				is = getInputStream(feed, feedFile.day, streamFileName, compressed)
			}
			
			if (is!=null) {
				// Write to disk before processing, then process from disk
				// Keep the same compressed state
				String cachedFileName = makeCacheFileName(feed, feedFile.day, streamFileName, compressed)
				Path path = Paths.get(cachedFileName);
				Files.createDirectories(path.getParent())

				FileOutputStream fileOut = new FileOutputStream(cachedFile)
				FileChannel fileChannel = fileOut.getChannel()

				Channel inChannel = Channels.newChannel(is)
				fileChannel.transferFrom(inChannel, 0L, Long.MAX_VALUE)
				inChannel.close() // closes the InputStream too
				fileChannel.close()
				fileOut.close()

				if (!cachedFile.canRead())
					throw new RuntimeException("Can not read the cached file: $cachedFileName")
				else return new StreamResponse(inputStream:new FileInputStream(cachedFile), stream:stream, feed:feed, day:feedFile.getDay(), success:true, fileSize: cachedFile.length(), isFile:true, isCompressed:compressed)
			}
			else {
				return new StreamResponse(success:false)
			}
		}
	}
	
	public void storeFile(File f, FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		getFileStorageAdapter().store(f, getCanonicalName(feedFile.feed, feedFile.day, f.name))
	}
	
	// Too slow to do this one by one
//	public void saveOrUpdateStream(Stream example, FeedFile feedFile) {
//		// Does it exist in the DB?
//		Stream db = Stream.findByFeedAndLocalId(feedFile.feed, example.localId)
//		if (!db) {
//			log.info("New Stream found from FeedFile $feedFile: $example")
//			db = example
//		}
//		
//		if (db.firstHistoricalDay==null || db.firstHistoricalDay.after(feedFile.day))
//			db.firstHistoricalDay = feedFile.day
//		if (db.lastHistoricalDay==null || db.lastHistoricalDay.before(feedFile.day))
//			db.lastHistoricalDay = feedFile.day
//
//		db.save(failOnError:true)
//	}
	
	
	
	public void saveOrUpdateStreams(List<Stream> foundStreams,
			FeedFile feedFile) {
		long time = System.currentTimeMillis()
		
		feedFile = FeedFile.get(feedFile.id)
		
		// Update the existing streams
		List<Stream> existing = Stream.findAllByFeedAndLocalIdInList(feedFile.feed, foundStreams.collect {it.localId})
		List existingIds = existing.collect {it.id}
		Stream.executeUpdate("update Stream s set s.firstHistoricalDay = :day where s.id in (:existingIds) and (s.firstHistoricalDay is null OR s.firstHistoricalDay > :day)", [day:feedFile.day, existingIds:existingIds])
		Stream.executeUpdate("update Stream s set s.lastHistoricalDay = :day where s.id in (:existingIds) and (s.lastHistoricalDay is null OR s.lastHistoricalDay < :day)", [day:feedFile.day, existingIds:existingIds])
		
		log.info("Checkpoint "+(System.currentTimeMillis()-time)+" ms")
		
		// Save the non-existing streams
		Set existingSet = new HashSet()
		existing.each {existingSet.add(it.localId)}
		foundStreams.each {Stream s->
			if (!existingSet.contains(s.localId)) {
				log.info("New Stream found from FeedFile $feedFile: $s")
				s.firstHistoricalDay = feedFile.day
				s.lastHistoricalDay = feedFile.day
				s.save(failOnError:true)
			}
		}
		log.info("Total "+(System.currentTimeMillis()-time)+" ms")
	}
	
	/**
	 * Queries the database for dates between beginDate and endDate for which
	 * data files exist (for the feeds in question).
	 * @param beginDate
	 * @param endDate
	 * @param feeds
	 * @return Date[2] arrays where [0] is the datafile day and [1] is that plus one day (minus one second)
	 */
	List<Date[]> getUnits(Date beginDate, Date endDate, List<Feed> feeds) {
		def c = FeedFile.createCriteria()
		
		List<FeedFile> files = c {
//			and {
				or {
					between("beginDate",beginDate,endDate)
					between("endDate",beginDate,endDate)
				}
//				'in'("feed",feeds) // FIXME: why doesn't this work?
//			}
			order("day","asc")
		} 
		
		List result = files.collect {FeedFile ff->
			Date[] d = new Date[2]
			d[0] = ff.beginDate
			d[1] = ff.endDate
			return d
		}
	}
	
	public class StreamResponse {
		InputStream inputStream
		Feed feed
		Stream stream
		Date day
		Boolean success
		Boolean isFile
		Boolean isCompressed
		Long fileSize
	}

	
}
