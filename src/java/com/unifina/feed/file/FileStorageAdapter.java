package com.unifina.feed.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import com.unifina.domain.data.Feed;

/**
 * This interface can be used to implement various file storage solutions
 * for different environments, for example local storage, http server or AWS S3
 * @author Henri
 */
public abstract class FileStorageAdapter {
	
	protected Map<String,Object> config;
	
	public FileStorageAdapter(Map<String,Object> config) {
		this.config = config;
	}
	
	public abstract InputStream retrieve(String canonicalPath) throws IOException;
	public abstract void store(File file, String canonicalPath) throws IOException;
}
