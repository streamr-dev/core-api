package com.unifina.service

import com.unifina.utils.HibernateHelper
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
import com.unifina.task.FeedFilePreprocessTask

class FeedFileService {

	GrailsApplication grailsApplication
	
	private static final Logger log = Logger.getLogger(FeedFileService.class)
	
	TaskService taskService
	
	FeedFile getFeedFile(Long id) {
		FeedFile.get(id)
	}
	
	/**
	 * Finds an existing FeedFile that matches the given criteria: 
	 * feed, name and timespan set by beginDate and endDate. Feed and
	 * name must exactly match, but it is enough for the FeedFile 
	 * beginDate and endDate to overlap with the given beginDate to endDate.
	 * 
	 * @param feed
	 * @param beginDate
	 * @param endDate
	 * @param name
	 * @return
	 */
	FeedFile getFeedFile(Feed feed, Date beginDate, Date endDate, String name, boolean matchName=true) {
		return FeedFile.withCriteria(uniqueResult:true) {
			eq("feed",feed)
			or {
				and {
					ge("beginDate", beginDate)
					lt("beginDate", endDate)
				}
				and {
					gt("endDate", beginDate)
					le("endDate", endDate)
				}
				and {
					lt("beginDate", beginDate)
					gt("endDate", endDate)
				}
			}
			if (matchName)
				eq("name",name)
		}
	}
	
	FeedFile getFeedFile(RemoteFeedFile file, boolean matchName=true) {
		return getFeedFile(file.feed, file.beginDate, file.endDate, file.name, matchName)
	}
	
	FeedFile createFeedFile(RemoteFeedFile remoteFile) {
		FeedFile feedFile = new FeedFile()
		feedFile.name = remoteFile.getName()
		feedFile.format = remoteFile.format
		
		feedFile.beginDate = remoteFile.getBeginDate()
		feedFile.endDate = remoteFile.getEndDate()
		// TODO: remove deprecated
		feedFile.day = remoteFile.getBeginDate()
		
		feedFile.processed = false
		feedFile.processing = true
		feedFile.processTaskCreated = true
		feedFile.feed = remoteFile.getFeed()
		feedFile.stream = remoteFile.getStreamId() != null ? Stream.load(remoteFile.getStreamId()) : null
		return feedFile
	}
	
	public FeedFile createFeedFile(Stream stream, Date beginDate, Date endDate, File file, boolean overwriteExisting) {
		// Check that the FeedFile does not exist
		FeedFile feedFile = getFeedFile(stream.feed, beginDate, endDate, file.getName())
		
		// If the FeedFile entry already exists, delete it
		if (feedFile && overwriteExisting) {
			log.info("Unprocessed FeedFile already exists. Deleting it and creating a new entry...")
			feedFile.delete()
		}
		else if (feedFile) {
			log.warn("FeedFile already exists: $feedFile.name, not overwriting.")
			return null
		}
		
		// Send file to file storage service if its size is greater than zero
		if (file.length()>0) {
			FeedFile.withTransaction {
				feedFile = new FeedFile()
				feedFile.name = file.getName()
				feedFile.beginDate = beginDate
				feedFile.endDate = endDate
				// TODO: remove deprecated
				feedFile.day = feedFile.beginDate
				
				feedFile.processed = false
				feedFile.processing = true
				feedFile.processTaskCreated = false
				feedFile.feed = stream.feed
				feedFile.stream = stream
				feedFile.save(flush:true, failOnError:true)
			}
			
			storeFile(file, feedFile)
			
			FeedFile.withTransaction {
				feedFile.attach()
				feedFile.processed = true
				feedFile.processing = false
				feedFile.save(flush:true, failOnError:true)
			}
			
			Stream.withTransaction {
				// Use pessimistic locking for updating the Stream
				stream = Stream.lock(stream.id)
				if (stream.firstHistoricalDay==null || stream.firstHistoricalDay.time > beginDate.time)
					stream.firstHistoricalDay = beginDate
				if (stream.lastHistoricalDay==null || stream.lastHistoricalDay.time < endDate.time)
					stream.lastHistoricalDay = endDate
					
				stream.save(flush:true, failOnError:true)
			}
			
			return feedFile
		}
		else return null
	}
	
	public void setPreprocessed(FeedFile feedFile) {
		feedFile = feedFile.merge()
		feedFile.processed = true
		feedFile.processing = false
		feedFile.save(failOnError:true)
	}
	
	/**
	 * Creates and saves a task to preprocess the given FeedFile.
	 * If a FeedFile entry already exists for the given remote file,
	 * a warning is logged and null is returned.
	 */
	public createPreprocessTask(RemoteFeedFile file, boolean download) {
		if (!getFeedFile(file)) {
			Task task = new Task(FeedFilePreprocessTask.class.getName(), (FeedFilePreprocessTask.getConfig(file, download) as JSON).toString(), "preprocess", taskService.createTaskGroupId())
			task.save(flush:true, failOnError:true)
			return task
		}
		else {
			log.warn("FeedFile already exists that matches the given RemoteFeedFile: $file. Returning null!")
			return null
		}
	}
	
	AbstractFeedPreprocessor getPreprocessor(Feed feed, String format="default") {
		if (feed.getPreprocessor()) {
			String className
			
			// Try format-specific preprocessor definition
			try {
				def json = JSON.parse(feed.getPreprocessor())
				className = json[format] ?: json["default"]
			} catch (Exception e) {}
			
			if (!className) {
				className = feed.getPreprocessor()
			}
			
			return this.getClass().getClassLoader().loadClass(className).newInstance()
		}
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
	
	/**
	 * Returns the default file storage adapter configured in Config.groovy as "unifina.feed.fileStorageAdapter"
	 */
	public FileStorageAdapter getFileStorageAdapter(String className = grailsApplication.config.unifina.feed.fileStorageAdapter) {
		if (!className)
			throw new RuntimeException("No default file storage adapter is configured and no classname was provided!")
			
		return this.getClass().getClassLoader().loadClass(className).newInstance(grailsApplication.config)
	}
	
	StreamResponse getFeed(FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		// First try compressed
		InputStream is = getInputStream(feedFile.feed, feedFile.day, feedFile.name, true)
		if (is!=null)
			return new StreamResponse(inputStream:is, feed:feedFile.feed, feedFile:feedFile, day:feedFile.getDay(), success:true, isFile:false, isCompressed:true)

		is = getInputStream(feedFile.feed, feedFile.day, feedFile.name, false)
		if (is!=null)
			return new StreamResponse(inputStream:is, feed:feedFile.feed, feedFile:feedFile, day:feedFile.getDay(), success:true, isFile:false, isCompressed:false)
			
		return new StreamResponse(success:false)
	}
	
	StreamResponse getStream(Stream stream, Date beginDate, Date endDate, int piece) {
		// Which feed?
		Feed feed = stream.getFeed()
		
		// Which feed file?
		FeedFile feedFile
		if (feed.bundledFeedFiles)
			feedFile = FeedFile.findByFeedAndDayBetween(feed, beginDate, endDate, [sort:'day', max:1, offset:piece])
		else 
			feedFile = FeedFile.findByStreamAndDayBetween(stream, beginDate, endDate, [sort:'day', max:1, offset:piece])
		
		// Null signals the end of data
		if (feedFile==null) {
			log.debug("getStream: no more FeedFiles for stream $stream.id, feed $stream.feed.id, beginDate: $beginDate, endDate: $endDate, piece: $piece")
			return null
		}
		else log.debug("getStream: starting FeedFile "+feedFile.id+" for stream "+stream.id)

		// Unproxy the feedFile.stream so it can be accessed from other threads with no bound session
		if (feedFile.stream)
			feedFile.stream = HibernateHelper.deproxy(feedFile.stream, Stream.class)

		// Instantiate preprocessor and get the preprocessed file name
		AbstractFeedPreprocessor preprocessor = getPreprocessor(feed)
		
		// streamFileName is the uncompressed name (without .gz suffix)
		String streamFileName = (preprocessor ? preprocessor.getPreprocessedFileName(feedFile.getName(), stream, false) : feedFile.getName())
		
		Boolean useCache = grailsApplication.config.unifina.feed.useCache
		
		if (!useCache) {
			// First try compressed version
			InputStream is = getInputStream(feed, feedFile.day, streamFileName, true)
			if (is!=null)
				return new StreamResponse(inputStream:is, stream:stream, feed:feed, feedFile:feedFile, day:feedFile.getDay(), success:true, isFile:false, isCompressed:true)
			
			is = getInputStream(feed, feedFile.day, streamFileName, false)
			if (is!=null)
				return new StreamResponse(inputStream:is, stream:stream, feed:feed, feedFile:feedFile, day:feedFile.getDay(), success:true, isFile:false, isCompressed:false)
		
			return new StreamResponse(success:false)
		}
		else {
			// Exists in cache compressed?
			File cachedFile = new File(makeCacheFileName(feed, feedFile.day, streamFileName, true))
			if (cachedFile.canRead()) {
				return new StreamResponse(inputStream:new FileInputStream(cachedFile), stream:stream, feed:feed, feedFile:feedFile, day:feedFile.getDay(), success:true, fileSize: cachedFile.length(), isFile:true, isCompressed:true)
			}
			// Exists in cache uncompressed?
			cachedFile = new File(makeCacheFileName(feed, feedFile.day, streamFileName, false))
			if (cachedFile.canRead()) {
				return new StreamResponse(inputStream:new FileInputStream(cachedFile), stream:stream, feed:feed, feedFile:feedFile, day:feedFile.getDay(), success:true, fileSize: cachedFile.length(), isFile:true, isCompressed:false)
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
				else return new StreamResponse(inputStream:new FileInputStream(cachedFile), stream:stream, feed:feed, feedFile:feedFile, day:feedFile.getDay(), success:true, fileSize: cachedFile.length(), isFile:true, isCompressed:compressed)
			}
			else {
				return new StreamResponse(success:false)
			}
		}
	}
	
	public void storeFile(File f, FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		String canonicalName = getCanonicalName(feedFile.feed, feedFile.day, f.name)
		log.debug("Storing $f to $canonicalName")
		getFileStorageAdapter().store(f, canonicalName)
	}
	
	public void deleteFile(FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		String canonicalName = getCanonicalName(feedFile.feed, feedFile.day, feedFile.name)
		log.debug("Deleting $canonicalName")
		getFileStorageAdapter().delete(canonicalName)
	}
	
	public void saveOrUpdateStreams(List<Stream> foundStreams,
			FeedFile feedFile) {
			
		// If no streams were found (for example, file contained no data), just return
		if (foundStreams.size()==0) {
			log.warn("No streams found in FeedFile $feedFile.name.")
			return
		}
			
		long time = System.currentTimeMillis()
		
		feedFile = FeedFile.get(feedFile.id)
		
		// Update the existing streams
		List<Stream> existing = Stream.findAllByFeedAndIdInList(feedFile.feed, foundStreams.collect {it.id})
		List existingIds = existing.collect {it.id}
		Stream.executeUpdate("update Stream s set s.firstHistoricalDay = :day where s.id in (:existingIds) and (s.firstHistoricalDay is null OR s.firstHistoricalDay > :day)", [day:feedFile.day, existingIds:existingIds])
		Stream.executeUpdate("update Stream s set s.lastHistoricalDay = :day where s.id in (:existingIds) and (s.lastHistoricalDay is null OR s.lastHistoricalDay < :day)", [day:feedFile.day, existingIds:existingIds])
		
		log.info("Checkpoint "+(System.currentTimeMillis()-time)+" ms")
		
		// Save the non-existing streams
		Set existingSet = new HashSet()
		existing.each {existingSet.add(it.id)}
		foundStreams.each {Stream s->
			if (!existingSet.contains(s.id)) {
				log.info("New Stream found from FeedFile $feedFile: $s")
				s.firstHistoricalDay = feedFile.day
				s.lastHistoricalDay = feedFile.day
				s.save(failOnError:true)
			}
		}
		log.info("Total "+(System.currentTimeMillis()-time)+" ms")
	}

	public FeedFile getFirstFeedFile(Stream stream) {
		return FeedFile.findByStream(stream, [sort:'beginDate', limit:1])
	}

	public FeedFile getLastFeedFile(Stream stream) {
		return FeedFile.findByStream(stream, [sort:'endDate', order:"desc", limit:1])
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
		FeedFile feedFile
		Stream stream
		Date day
		Boolean success
		Boolean isFile
		Boolean isCompressed
		Long fileSize
	}

	
}
