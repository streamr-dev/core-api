package com.unifina.signalpath;

import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;

import java.util.*;

public abstract class AbstractStreamSourceModule extends AbstractSignalPathModule {

	private final StreamParameter streamParameter = new StreamParameter(this, "stream");

	@Override
	public void init() {
		streamParameter.setUpdateOnChange(true);
		streamParameter.setDrivingInput(false);
		streamParameter.setCanToggleDrivingInput(false);
		addInput(streamParameter);
	}

	public Stream getStream() {
		return streamParameter.getValue();
	}

	/**
	 * TODO: add mechanism for selecting which partitions are needed.
	 * Current implementation wants all partitions.
	 */

	public Collection<Integer> getPartitions() {
		List<Integer> arr = new ArrayList<>(getStream().getPartitions());
		for (int i=0; i<getStream().getPartitions(); i++) {
			arr.add(i);
		}
		return arr;
	}

	public Collection<StreamPartition> getStreamPartitions() {
		List<StreamPartition> arr = new ArrayList<>(getStream().getPartitions());
		for (int i=0; i<getStream().getPartitions(); i++) {
			arr.add(new StreamPartition(getStream().getId(), i));
		}
		return arr;
	}
}
