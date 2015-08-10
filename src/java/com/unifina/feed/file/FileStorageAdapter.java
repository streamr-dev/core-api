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
	 * identified by location. It will return null if the resource was
	 * not found or could not be retrieved after a number of retries.
	 * @param location
	 * @return
	 */
	public InputStream retrieve(String location) {
		Exception ex = null;
		for (int i=1;i<=retries;i++) {
			InputStream is = null;
			try {
				is = tryRetrieve(location);
				return is;
			} catch (FileNotFoundException e) {
				log.warn("Resource not found: "+location+", returning null");
				return null;
			} catch (Exception e) {
				log.warn("Failed to retrieve "+location+", attempt "+i);
				ex = e;
			}
		}
		log.error("Failed to retrieve "+location+", giving up. Latest exception was: ",ex);
		return null;
	}
	
	/**
	 * Attempts to store a File to the specified location. Will throw an
	 * IOException if not successful after a number of retries.
	 * @param file
	 * @param location
	 */
	public void store(File file, String location) throws IOException {
		for (int i=1;i<=retries;i++) {
			try {
				tryStore(file, location);
				return;
			} catch (Exception e) {
				log.error("Failed to store "+file+" to "+location+", attempt "+i,e);
			}
		}
		throw new IOException("Failed to store "+file+" to "+location+", attempts exhausted");
	}
	
	/**
	 * Attempts to delete a File from the specified location. Will throw an
	 * IOException if not successful after a number of retries.
	 * @param file
	 * @param location
	 */
	public void delete(String location) throws IOException {
		for (int i=1;i<=retries;i++) {
			try {
				tryDelete(location);
				return;
			} catch (Exception e) {
				log.error("Failed to delete "+location+", attempt "+i,e);
			}
		}
		throw new IOException("Failed to delete "+location+", attempts exhausted");
	}
	
	protected abstract InputStream tryRetrieve(String location) throws IOException;
	protected abstract void tryStore(File file, String location) throws IOException;
	protected abstract void tryDelete(String location) throws IOException;
}
