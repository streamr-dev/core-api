package com.unifina.utils.testutils;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.signalpath.remote.ConnexorTweetSentiment;

import java.util.HashMap;
import java.util.Map;

public class FakeConnexorTweetSentiment extends ConnexorTweetSentiment {

	private static final Map<String, String> responses = new HashMap<>();

	static {
		responses.clear();
		responses.put("paska", "<sentiment> NEG </sentiment>");
		responses.put("hieman huonohko", "<sentiment> (NEG) </sentiment>");
		responses.put("ok", "<sentiment> (POS) </sentiment>");
		responses.put("hyvä", "<sentiment> POS </sentiment>");
	}

	@Override
	protected String login(String email, String password, String url) throws UnirestException {
		return "";
	}

	@Override
	protected String postTweet(String tweet) throws UnirestException {
		return responses.get(tweet);
	}
}
