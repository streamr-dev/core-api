package com.unifina.data;

import com.unifina.domain.data.Stream;

/**
 * Should be implemented by AbstractSignalPathModules whose presence
 * in a SignalPath indicates that a certain Stream is required.
 * @author Henri
 */
public interface IStreamRequirement {
	public Stream getStream();
}
