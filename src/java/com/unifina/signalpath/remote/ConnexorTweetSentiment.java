package com.unifina.signalpath.remote;

import org.apache.log4j.Logger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.mashape.unirest.http.Unirest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnexorTweetSentiment extends AbstractSignalPathModule {

	StringInput tweet = new StringInput(this, "tweet");
	TimeSeriesOutput out = new TimeSeriesOutput(this, "sentiment");

	private String authKey = "gmY9pmyauSxmDn2b02to";

	private static final Logger log = Logger.getLogger(ConnexorTweetSentiment.class); 

	private static final String connexorRoot = "http://cxserv1.nlpengine.net/serv/stable/media";
	private static final String tweetApiUrl = connexorRoot + "/tweet/";
	private static final Pattern sentimentRe = Pattern.compile(".*<sentiment> (.*) </sentiment>.*", Pattern.MULTILINE | Pattern.DOTALL);

	public void initialize() {
		String email = "nikke.nylund@unifina.com";
		String password = "xahtiGhahc8h";

		String url = connexorRoot + "/login";

		try {
			String resp = Unirest.post(url)
				.field("Email", email)
				.field("Password", password)
				.asString()
				.getBody();

			Pattern p = Pattern.compile(".*session key=\"(.*)\".*", Pattern.MULTILINE | Pattern.DOTALL);
			Matcher m = p.matcher(resp);

			if (m.find())
				authKey = m.group(1);
		} catch(Exception e) {
			log.error("Exception: ", e);
			throw new RuntimeException("Failed to login to Connexor", e);
		}
	}

	public void clearState() {}
	
	public void sendOutput() {
		String twit = tweet.getValue();

		try {
			String resp = Unirest.post(tweetApiUrl)
				.field("tweet1", twit)
				.field("AuthKey", authKey)
				.asString()
				.getBody();

			Matcher m = sentimentRe.matcher(resp);

			double connexorSentiment = 0;
			
			if (!m.find()) {
				log.warn("No sentiment found "+resp);
				return;
			}
			
			String sentiment = m.group(1);
			
			switch(sentiment) {
				case "(POS)":
					connexorSentiment = 0.5;
					break;
				case "POS":
					connexorSentiment = 1;
					break;
				case "(NEG)":
					connexorSentiment = -0.5;
					break;
				case "NEG":
					connexorSentiment = -1;
					break;
			}

			out.send(connexorSentiment);
		} catch (Exception e) {
			log.error("Exception: ", e);
			out.send(0);
		}
	}
	
}
