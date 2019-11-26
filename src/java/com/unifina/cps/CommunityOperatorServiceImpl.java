package com.unifina.cps;

import com.unifina.api.ProxyException;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Engine and editor proxies the following endpoints to the Community Product server:
 * <p>
 * GET /communities/{communityAddress}/stats: returns Operator stats.
 * GET /communities/{communityAddress}/members: returns list of members
 * GET /communities/{communityAddress}/members/{memberAddress}: returns individual member stats (such as balances and withdraw proofs)
 */
public class CommunityOperatorServiceImpl implements CommunityOperatorService {
	private static final Logger log = LogManager.getLogger(CommunityOperatorServiceImpl.class);
	private String baseUrl;
	private CloseableHttpClient client;

	public CommunityOperatorServiceImpl() {
		this(20 * 1000, 50 * 1000, 20 * 1000, 100, 100);
	}

	public CommunityOperatorServiceImpl(int connectTimeout, int connectionRequestTimeout, int socketTimeout, int maxConnTotal, int maxConnPerRoute) {
		this.baseUrl = MapTraversal.getString(Holders.getConfig(), "streamr.cps.url");
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(connectTimeout)
			.setConnectionRequestTimeout(connectionRequestTimeout)
			.setSocketTimeout(socketTimeout)
			.build();
		this.client = HttpClientBuilder.create()
			.setDefaultRequestConfig(config)
			.setMaxConnTotal(maxConnTotal)
			.setMaxConnPerRoute(maxConnPerRoute)
			.build();
	}

	protected ProxyResponse proxy(String url) {
		log.debug("entering proxy");
		long start = System.currentTimeMillis();
		ProxyResponse result = new ProxyResponse();
		CloseableHttpResponse response = null;
		try {
			HttpGet req = new HttpGet(url);
			req.setHeader(HttpHeaders.ACCEPT, "application/json");
			try {
				response = client.execute(req);
			} catch (ConnectException e) {
				String msg = "Community server is not responding";
				log.error(msg, e);
				throw new ProxyException(msg);
			} catch (SocketTimeoutException e) {
				String msg = "Community server gateway timeout";
				log.error(msg, e);
				throw new ProxyException(504, msg);
			} catch (IOException e) {
				log.error("http client io error", e);
				throw new ProxyException(e.getMessage());
			}

			StatusLine statusLine = response.getStatusLine();
			result.setStatusCode(statusLine.getStatusCode());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					result.setBody(EntityUtils.toString(entity, StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new ProxyException(e.getMessage());
				}
			}
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					log.info("Failed to close http client response to cps.", e);
				}
			}
		}
		long total = System.currentTimeMillis() - start;
		log.info("CommunityOperatorServiceImpl.proxy() execution took " + total + " ms");
		log.debug("exiting proxy");
		return result;
	}

	public void close() throws IOException {
		if (this.client != null) {
			client.close();
		}
	}

	public ProxyResponse stats(String communityAddress) {
		String url = String.format("%s%s/stats", baseUrl, communityAddress);
		return proxy(url);
	}

	public ProxyResponse members(String communityAddress) {
		//String url = String.format("%s%s/members", baseUrl, communityAddress);
		String url = "http://localhost:4444";
		return proxy(url);
	}

	public ProxyResponse memberStats(String communityAddress, String memberAddress) {
		String url = String.format("%s%s/members/%s", baseUrl, communityAddress, memberAddress);
		return proxy(url);
	}

	public static class ProxyResponse {
		private String body = "";
		private int statusCode;

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
	}
}
