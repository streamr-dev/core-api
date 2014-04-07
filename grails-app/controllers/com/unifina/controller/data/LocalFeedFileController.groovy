package com.unifina.controller.data

import java.nio.channels.Channel
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import org.apache.log4j.Logger

/**
 * This controller can be used to request and save files from a local filesystem.
 * The directory is specified with grailsApplication.config.unifina.feed.datadir.
 * 
 * It must NOT be used in a production environment as it does not check for
 * any authentication. To disable this functionality, don't specify the datadir in Config.
 * @author Henri
 */
class LocalFeedFileController {
	
	String sep = System.getProperty("file.separator")
	
	def grailsApplication
	
	private static final Logger log = Logger.getLogger(LocalFeedFileController.class)
	
	def index() {
		if (grailsApplication.config.unifina.feed.datadir) {
			if (!params.feedDir || !params.day || !params.file) {
				response.contentType = "text/plain"
				response.status = 400
				render "Required params: feedDir, day, file"
			}
			else {
				String filename = "${grailsApplication.config.unifina.feed.datadir}${sep}${params.feedDir}${sep}${params.day}${sep}${params.file}"
				Path path = Paths.get(filename)
		
				// GET to load a file
				if (request.method=="GET") {		
					if (Files.isReadable(path)) {
						response.contentType = "application/octet-stream"
						
						FileChannel fileChannel = FileChannel.open(path)
						Channel outChannel = Channels.newChannel(response.outputStream)
						fileChannel.transferTo(0, Long.MAX_VALUE, outChannel)
						fileChannel.close()
						response.outputStream.flush()
					}
					else {
						response.contentType = "text/plain"
						response.status = 404
						render "Data file not found"
					}
				}
				// POST to save a file (very unsafe, use for environment mocking only)
				else if (request.method=="POST") {
//					log.info("Writing file to "+path)
					if (!Files.exists(path.getParent()))
						Files.createDirectories(path.getParent())
						
					FileChannel fileChannel = FileChannel.open(path, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE))
					Channel inChannel = Channels.newChannel(request.inputStream)
					fileChannel.transferFrom(inChannel, 0, Long.MAX_VALUE)
					fileChannel.close()
					response.contentType = "text/plain"
					response.status = 200
					render "OK"
				}
			}
		}
		// If unifina.feed.datadir not defined
		else {
			response.contentType = "text/plain"
			response.status = 404
			render ""
		}
	}
}
