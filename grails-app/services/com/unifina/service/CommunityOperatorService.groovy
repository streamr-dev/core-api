package com.unifina.service

import com.unifina.api.ProxyException
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets

/**
 * Engine and editor proxies the following endpoints to the Community Product server:
 *
 * GET /communities/{communityAddress}/stats: returns Operator stats.
 * GET /communities/{communityAddress}/members: returns list of members
 * GET /communities/{communityAddress}/members/{memberAddress}: returns individual member stats (such as balances and withdraw proofs)
 */
class CommunityOperatorService {
	String baseUrl
	HttpClient client

	private static final TIMEOUT_SECONDS = 10

	CommunityOperatorService() {
		this.baseUrl = MapTraversal.getString(Holders.getConfig(), "streamr.cps.url");
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(TIMEOUT_SECONDS * 1000)
			.setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000)
			.setSocketTimeout(TIMEOUT_SECONDS * 1000)
			.build()

		this.client = HttpClientBuilder.create()
			.setDefaultRequestConfig(config)
			.build()
	}

	static class ProxyResponse {
		String body = ""
		int statusCode
	}

	protected ProxyResponse proxy(String url) {
		ProxyResponse result = new ProxyResponse()
		CloseableHttpResponse response
		try {
			HttpGet req = new HttpGet(url)
			req.setHeader(HttpHeaders.ACCEPT, "application/json")
			try {
				response = client.execute(req)
			} catch (ConnectException e) {
				throw new ProxyException("Community server is not responding")
			}
			StatusLine statusLine = response.getStatusLine()
			result.statusCode = statusLine.getStatusCode()
			HttpEntity entity = response.getEntity()
			if (entity != null) {
				result.body = EntityUtils.toString(entity, StandardCharsets.UTF_8)
			}
		} finally {
			if (response != null) {
				response.close()
			}
		}
		return result
	}

	ProxyResponse stats(String communityAddress) {
		String url = String.format("%s%s%s", baseUrl, communityAddress, "/stats")
		return proxy(url)
	}

	ProxyResponse members(String communityAddress) {
		String url = String.format("%s%s%s", baseUrl, communityAddress, "/members")
		return proxy(url)
	}

	ProxyResponse memberStats(String communityAddress, String memberAddress) {
		String url = String.format("%s%s%s%s", baseUrl, communityAddress, "/members/", memberAddress)
		return proxy(url)
	}
}
