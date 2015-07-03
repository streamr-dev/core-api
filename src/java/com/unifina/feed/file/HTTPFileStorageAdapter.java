package com.unifina.feed.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.unifina.utils.MapTraversal;

/**
 * FileStorageAdapter implementation that fetches the requested files
 * via HTTP get and stores them via HTTP put.
 * 
 * Intended to be used with FeedFileController, but should work with
 * any server that implements a similar scheme.
 * @author Henri
 *
 */
public class HTTPFileStorageAdapter extends FileStorageAdapter {

	String serverPrefix;
	
	public HTTPFileStorageAdapter(Map<String, Object> config) {
		super(config);
		serverPrefix = MapTraversal.getString(config, "unifina.feed.httpFileStorageAdapter.prefix");
		if (serverPrefix==null)
			throw new RuntimeException("Server prefix is not configured for HTTPFileStorageAdapter!");
	}

	protected String encodeLocation(String location) {
		// URL encode parts of the path
		StringBuilder cp = new StringBuilder();
		try {
			for (String s : location.split("/")) {
				if (!s.equals("")) {
					cp.append("/");
					cp.append(URLEncoder.encode(s, "UTF-8"));
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return cp.toString();
	}
	
	protected URL makeURL(String location) throws MalformedURLException {
		location = encodeLocation(location);
		
		StringBuilder sb = new StringBuilder();
		sb.append(serverPrefix.endsWith("/") ? serverPrefix.substring(0, serverPrefix.length()-1)  : serverPrefix);
		sb.append("/");
		sb.append(location.startsWith("/") ? location.substring(1) : location);
		return new URL(sb.toString());
	}
	
	@Override
	protected InputStream tryRetrieve(String location) throws IOException {
		URL url = makeURL(location);
//		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		int statusCode = -1;
		try {
			client = HttpClients.createDefault();
			HttpGet get = new HttpGet(url.toURI());
			response = client.execute(get);
			statusCode = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			throw new IOException(e);
		}
		// TODO: how to close the client? It can't be closed yet as the stream is needed
				
		// Server may return compressed or uncompressed, this is communicated in contentType
		if (statusCode==200) {
			return response.getEntity().getContent();
		}
		else if (statusCode==404) {
			client.close();
			response.close();
			return null;
		}
		else throw new RuntimeException("Unhandled status code from data server: "+statusCode);
	}

	@Override
	protected void tryStore(File file, String location) throws IOException {
		URL url = makeURL(location);
		
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			client = HttpClients.createDefault();
			HttpPost post = new HttpPost(url.toURI());
			FileEntity fe = new FileEntity(file, ContentType.create("application/octet-stream"));
			post.setEntity(fe);
			response = client.execute(post);
			if (response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK)
				throw new RuntimeException("Store POST to "+url+" resulted in status code: "+response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (response!=null)
				response.close();
			if (client!=null)
				client.close();
		}
	}
	
	@Override
	protected void tryDelete(String location) throws IOException {
		URL url = makeURL(location);
		
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			client = HttpClients.createDefault();
			HttpDelete delete = new HttpDelete(url.toURI());
			response = client.execute(delete);
			if (response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK)
				throw new RuntimeException("DELETE to "+url+" resulted in status code: "+response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (response!=null)
				response.close();
			if (client!=null)
				client.close();
		}
	}
}
