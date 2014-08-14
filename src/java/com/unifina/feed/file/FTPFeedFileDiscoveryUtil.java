package com.unifina.feed.file;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.data.Feed;

public abstract class FTPFeedFileDiscoveryUtil extends AbstractFeedFileDiscoveryUtil {

	private static final Logger log = Logger.getLogger(FTPFeedFileDiscoveryUtil.class);
	
	private List<String> remoteDirectories;
	private String username;
	private String password;
	private String server;
	
	private FTPClient ftp;
	
	public FTPFeedFileDiscoveryUtil(GrailsApplication grailsApplication,
			Feed feed, Map<String, Object> config) {
		super(grailsApplication, feed, config);
		
		remoteDirectories = (List<String>) config.get("remoteDirectories");
		username = (String) config.get("username");
		password = (String) config.get("password");
		server = (String) config.get("server");

	}

	@Override
	protected void connect() {
		ftp = new FTPClient();
	    FTPClientConfig config = new FTPClientConfig();
	    ftp.configure(config);
	    
	    try {
	    	log.info("Connecting to FTP server: "+server+"...");
			ftp.connect(server);
			log.info("Sending username and password...");
			ftp.login(username, password);
			log.info(ftp.getReplyString());
			
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				ftp.disconnect();
				log.error("FTP server refused connection.");
				throw new RuntimeException("FTP server refused connection.");
			}
		
			log.info("Entering passive mode..");
			ftp.enterLocalPassiveMode();
			log.info(ftp.getReplyString());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void disconnect() {
		try {
			log.info("Disconnecting...");
			ftp.disconnect();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected URL getURL(String remoteDir, FTPFile file) {
		try {
			return new URL(new StringBuilder()
			.append("ftp://").append(username).append(":").append(password).append("@")
			.append(server).append(remoteDir).append("/").append(file.getName()).toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected List<String> listFiles() {
		log.info("Scanning for files on server...");
		List<String> result = new ArrayList<>();
		
		for (String remoteDir : remoteDirectories) {
			FTPFile[] files;
			try {
				log.info("Listing files from "+remoteDir+"...");
				files = ftp.listFiles(remoteDir);
				log.info(files.length+" files found.");
			} catch (IOException e) {
				log.error("Failed to get files for remote directory: "+remoteDir);
				files = new FTPFile[0];
			}
			
			for (FTPFile file : files) {
				result.add(getURL(remoteDir,file).toString());
			}
		}
		
		return result;
	}
	
}
