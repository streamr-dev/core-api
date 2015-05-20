package com.unifina.service

import grails.converters.JSON

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator

class StreamService {
	Stream createUserStream(params, SecUser user) {
		Stream stream = new Stream(params)
		stream.uuid = IdGenerator.get()
		stream.apiKey = IdGenerator.get()
		stream.user = user
		if (stream.localId==null)
			stream.localId = stream.name
		
		stream.feed = Feed.load(7) // API stream
		stream.streamConfig = ([fields:[], topic: stream.uuid] as JSON)
		
		stream.save()
		
		return stream
	}
}
