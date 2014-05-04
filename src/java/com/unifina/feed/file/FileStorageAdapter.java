package com.unifina.feed.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This interface can be used to implement various file storage solutions
 * for different environments, for example local storage, http server or AWS S3
 * @author Henri
 */
public abstract class FileStorageAdapter {
	
	protected Map<String,Object> config;
	private int retries = 5;
	
	private static final Logger log = Logger.getLogger(FileStorageAdapter.class);
	
	public FileStorageAdapter(Map<String,Object> config) {
		this.config = config;
		if (config.containsKey("retries"))
			retries = (int) config.get("retries");
	}
	
	/**
	 * Attempts to retrieve an InputStream from a resource
	 * identified by canonicalPath. It will return null if the resource was
	 * not found or could not be retrieved after a number of retries.
	 * @param canonicalPath
	 * @return
	 */
	public InputStream retrieve(String canonicalPath) {
		Exception ex = null;
		for (int i=1;i<=retries;i++) {
			InputStream is = null;
			try {
				is = tryRetrieve(canonicalPath);
				return is;
			} catch (FileNotFoundException e) {
				log.warn("Resource not found: "+canonicalPath+", returning null");
				return null;
			} catch (Exception e) {
				log.warn("Failed to retrieve "+canonicalPath+", attempt "+i);
				ex = e;
			}
		}
		log.error("Failed to retrieve "+canonicalPath+", giving up. Latest exception was: ",ex);
		return null;
	}
	
	/**
	 * Attempts to store a File to the specified canonicalPath. Will throw an
	 * IOException if not successful after a number of retries.
	 * @param file
	 * @param canonicalPath
	 */
	public void store(File file, String canonicalPath) throws IOException {
		for (int i=1;i<=retries;i++) {
			try {
				tryStore(file, canonicalPath);
				return;
			} catch (Exception e) {
				log.error("Failed to store "+file+" to "+canonicalPath+", attempt "+i,e);
			}
		}
		throw new IOException("Failed to store "+file+" to "+canonicalPath+", attempts exhausted");
	}
	
	protected abstract InputStream tryRetrieve(String canonicalPath) throws IOException;
	protected abstract void tryStore(File file, String canonicalPath) throws IOException;
}
