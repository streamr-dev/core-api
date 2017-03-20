package com.unifina.data;

import com.unifina.domain.data.Stream;

import java.util.Set;

/**
 * Should be implemented by AbstractSignalPathModules whose presence
 * in a SignalPath indicates that a certain Stream is required.
 * @author Henri
 */
public interface IStreamRequirement {
	/**
	 * Returns the Stream required by this instance
     */
	public Stream getStream();

	/**
	 * Returns the partitions required by this instance
     */
	public Set<Integer> getPartitions();
}
