package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.feed.file.FileStorageAdapter
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.text.SimpleDateFormat

class FeedFileService {

	GrailsApplication grailsApplication
	
	private static final Logger log = Logger.getLogger(FeedFileService.class)

	private String getCanonicalName(Feed feed, Date day, String filename) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd")
		return "${feed.directory}/${df.format(day)}/${filename}"
	}
	
	/**
	 * Returns the default file storage adapter configured in Config.groovy as "unifina.feed.fileStorageAdapter"
	 */
	private FileStorageAdapter getFileStorageAdapter(String className = grailsApplication.config.unifina.feed.fileStorageAdapter) {
		if (!className) {
			throw new RuntimeException("No default file storage adapter is configured and no classname was provided!")
		}
		return this.getClass().getClassLoader().loadClass(className).newInstance(grailsApplication.config)
	}

	void storeFile(File f, FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		String canonicalName = getCanonicalName(feedFile.feed, feedFile.day, f.name)
		log.debug("Storing $f to $canonicalName")
		getFileStorageAdapter().store(f, canonicalName)
	}

	void deleteFile(FeedFile feedFile) {
		feedFile = FeedFile.get(feedFile.id)
		String canonicalName = getCanonicalName(feedFile.feed, feedFile.day, feedFile.name)
		log.debug("Deleting $canonicalName")
		getFileStorageAdapter().delete(canonicalName)
	}
}
