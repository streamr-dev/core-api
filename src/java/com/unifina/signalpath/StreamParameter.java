package com.unifina.signalpath;

import java.util.Map;

import com.unifina.domain.data.Stream;
import com.unifina.service.FeedService;

// This class can also receive Strings for backwards compatibility 
public class StreamParameter extends Parameter<Stream> {
	
	private boolean checkModuleId = false;
	
	public StreamParameter(AbstractSignalPathModule owner, String name) {
		super(owner,name,null,"Stream");
		this.canToggleDrivingInput = false;
	}

	@Override
	protected String[] getAcceptedTypes() {
		return new String[] {"Stream","String"};
	}
	
	@Override
	public void receive(Object value) {
		if (value instanceof String) {
			FeedService fs = (FeedService) owner.globals.getGrailsApplication().getMainContext().getBean("feedService");
			value = fs.getStream((String)value);
		}
		super.receive(value);
	}
	
	@Override
	protected Stream handlePulledObject(Object o) {
		if (o instanceof String) {
			FeedService fs = (FeedService) owner.globals.getGrailsApplication().getMainContext().getBean("feedService");
			return fs.getStream((String)o);
		}
		else return super.handlePulledObject(o);
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		if (value!=null) {
			config.put("value",value.getId());
			config.put("streamName", value.getName());
			config.put("feed",value.getFeed().getId());
			if (checkModuleId) {
				config.put("checkModuleId",true);
//				config.put("moduleId", value.getFeed().getModule().getId());
			}
		}
		return config;
	}
	
	@Override
	public Stream parseValue(String s) {
		if (s==null || s.equals("null"))
			return null;
		
		FeedService fs = (FeedService) owner.globals.getGrailsApplication().getMainContext().getBean("feedService");
		
		Stream result; 
		try {
			result = fs.getStream(Long.parseLong(s));
			return result;
		} catch (NumberFormatException e) {}
		
		return fs.getStream(s);
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
	
}
