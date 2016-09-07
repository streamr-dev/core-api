package com.unifina.signalpath;

import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.StreamNotFoundException;
import com.unifina.service.FeedService;

import java.util.Map;

// This class can also receive Strings for Stream UUID
public class StreamParameter extends Parameter<Stream> {

	private boolean checkModuleId = false;
	private Feed feedFilter = null;

	public StreamParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "Stream");
		this.canToggleDrivingInput = false;
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
			config.put("feed", value.getFeed().getId());
			if (checkModuleId) {
				config.put("checkModuleId", true);
			}
		}

		if (feedFilter != null) {
			config.put("feedFilter", feedFilter.getId());
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

	private Stream getStreamById(Object id) {
		if (id instanceof String) {
			FeedService fs = getFeedService();
			try {
				return fs.getStream((String) id);
			} catch (StreamNotFoundException e) {
				throw new ModuleCreationFailedException(e);
			}
		} else if (id instanceof Number) {
			throw new RuntimeException("Numeric stream ids no longer supported");
		}
		return (Stream) id;
	}

	private FeedService getFeedService() {
		return getOwner().getGlobals().getBean(FeedService.class);
	}

	public boolean getCheckModuleId() {
		return checkModuleId;
	}

	/**
	 * Warns in the UI if such a stream is selected that the current module is
	 * not the implementing module for that stream. Set this to true on source modules.
	 * @param checkModuleId
	 */
	public void setCheckModuleId(boolean checkModuleId) {
		this.checkModuleId = checkModuleId;
	}

	public Feed getFeedFilter() {
		return feedFilter;
	}

	public void setFeedFilter(Feed feedFilter) {
		this.feedFilter = feedFilter;
	}

}
