package com.unifina.feed;

import com.unifina.domain.data.Stream;
import com.unifina.utils.Globals;

/**
 * This is a superclass for AbstractEventRecipients that can be automatically
 * instantiated in AbstractFeed. The class name can be defined in the database.
 * @author Henri
 *
 * @param <ModuleClass>
 * @param <MessageClass>
 */
public abstract class StreamEventRecipient<ModuleClass, MessageClass extends ITimestamped> extends AbstractEventRecipient<ModuleClass, MessageClass> {

	private Stream stream;

	public StreamEventRecipient(Globals globals, Stream stream) {
		super(globals);
		this.stream = stream;
	}

	public Stream getStream() {
		return stream;
	}

}
