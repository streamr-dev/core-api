package com.unifina.feed;

import com.unifina.domain.data.Stream;
import com.unifina.utils.Globals;

import java.util.Set;

/**
 * This is a superclass for AbstractEventRecipients that can be automatically
 * instantiated in AbstractFeed. The class name can be defined in the database.
 * @author Henri
 *
 * @param <ModuleClass>
 * @param <MessageClass>
 */
public abstract class StreamEventRecipient<ModuleClass, MessageClass extends ITimestamped> extends AbstractEventRecipient<ModuleClass, MessageClass> {

	private final Stream stream;
	private final Set<Integer> partitions;

	public StreamEventRecipient(Globals globals, Stream stream, Set<Integer> partitions) {
		super(globals);
		this.stream = stream;
		this.partitions = partitions;
	}

	public Stream getStream() {
		return stream;
	}

	public Set<Integer> getPartitions() {
		return partitions;
	}

}
