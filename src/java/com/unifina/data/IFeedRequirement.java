package com.unifina.data;

import com.unifina.domain.data.Feed;

/**
 * Should be implemented by AbstractSignalPathModules whose presence
 * in a SignalPath indicates that a certain Feed is required (no specific Stream).
 * @author Henri
 */
public interface IFeedRequirement {
	public Feed getFeed();
}
