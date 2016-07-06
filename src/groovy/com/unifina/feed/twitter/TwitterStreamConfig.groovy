package com.unifina.feed.twitter

import com.unifina.domain.data.Stream
import com.unifina.exceptions.InvalidStreamConfigException
import grails.validation.Validateable

@Validateable
class TwitterStreamConfig {
	public static final String consumerKey = "mosTwR1X0EgiR9lB81EGhYRrP"
	public static final String consumerSecret = "W9G6fBWYCy4ywMJpG3TWrgj5LtHv0h4e5c4dmEQbc8BGdSTSaj"

	String accessToken
	String accessTokenSecret

	String keywordsToTrack
	def List<String> getKeywords() { keywordsToTrack.split(",")*.trim() }

	static constraints = {
		keywordsToTrack(blank: false)
	}

	static TwitterStreamConfig fromStreamOrEmpty(Stream stream) {
		try {
			return fromStream(stream)
		} catch (InvalidStreamConfigException e) {
			return new TwitterStreamConfig()
		}
	}

	static TwitterStreamConfig fromStream(Stream stream) {
		def conf = stream.getStreamConfigAsMap()["twitter"]
		if (conf == null) {
			throw new InvalidStreamConfigException("Stream " + stream.getId() + " config does not contain the 'twitter' key!");
		}

		TwitterStreamConfig config = new TwitterStreamConfig(conf)
		if (!config.validate()) {
			throw new InvalidStreamConfigException("Stream " + stream.getId() + " does not have a valid Twitter stream configuration!");
		}

		return config
	}

}
