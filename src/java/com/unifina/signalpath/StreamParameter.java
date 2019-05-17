package com.unifina.signalpath;

import com.unifina.domain.data.Stream;
import com.unifina.exceptions.StreamNotFoundException;
import com.unifina.service.StreamService;
import grails.util.Holders;

import java.util.Map;

// This class can also receive Strings for Stream UUID
public class StreamParameter extends Parameter<Stream> {

	public StreamParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "Stream");
		this.setCanToggleDrivingInput(false);
	}

	@Override
	protected String[] getAcceptedTypes() {
		return new String[] {"Stream", "String"};
	}

	@Override
	public void receive(Object value) {
		super.receive(getStreamById(value));
	}

	@Override
	protected Stream handlePulledObject(Object o) {
		if (o instanceof Stream) {
			return super.handlePulledObject(o);
		} else {
			return getStreamById(o);
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		if (value != null) {
			config.put("value", value.getId());
			config.put("streamName", value.getName());
		}
		return config;
	}

	@Override
	public Stream parseValue(String s) {
		if (s == null || s.equals("null")) {
			return null;
		}
		return getStreamById(s);
	}

	@Override
	public Object formatValue(Stream value) {
		return value == null ? null : value.getId(); // Controls how value and defaultValue are turned to config
	}

	private Stream getStreamById(Object idOrStream) {
		if (idOrStream == null) {
			return null;
		} else if (idOrStream instanceof Stream) {
			return (Stream) idOrStream;
		}

		StreamService ss = Holders.getApplicationContext().getBean(StreamService.class);
		try {
			return ss.getStream(idOrStream.toString());
		} catch (StreamNotFoundException e) {
			throw new ModuleCreationFailedException(e);
		}
	}
}
