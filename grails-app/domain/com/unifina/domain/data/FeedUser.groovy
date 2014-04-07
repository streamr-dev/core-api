package com.unifina.domain.data

import org.apache.commons.lang.builder.HashCodeBuilder

import com.unifina.domain.security.SecUser

class FeedUser implements Serializable {
	SecUser user
	Feed feed
	
	static mapping = {
		id composite: ['user', 'feed']
		version false
	}
	
	boolean equals(other) {
		if (!(other instanceof FeedUser)) {
			return false
		}

		other.user?.id == user?.id &&
			other.feed?.id == feed?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (feed) builder.append(feed.id)
		builder.toHashCode()
	}
}
