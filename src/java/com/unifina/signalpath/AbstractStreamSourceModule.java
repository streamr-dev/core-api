package com.unifina.signalpath;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Stream;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractStreamSourceModule extends AbstractSignalPathModule implements IStreamRequirement {

	private final StreamParameter streamParameter = new StreamParameter(this, "stream");

	@Override
	public void init() {
		streamParameter.setCheckModuleId(true);
		streamParameter.setUpdateOnChange(true);
		streamParameter.setDrivingInput(false);
		streamParameter.setCanToggleDrivingInput(false);
		addInput(streamParameter);
	}
	
	@Override
	public Stream getStream() {
		return streamParameter.getValue();
	}

	/**
	 * TODO: add mechanism for selecting which partitions are needed.
	 * Current implementation wants all partitions.
	 * @return
	 */
	@Override
	public Set<Integer> getPartitions() {
		Set<Integer> arr = new HashSet<>(getStream().getPartitions());
		for (int i=0; i<getStream().getPartitions(); i++) {
			arr.add(i);
		}
		return arr;
	}

}
