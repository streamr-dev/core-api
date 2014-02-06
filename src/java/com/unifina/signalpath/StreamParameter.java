package com.unifina.signalpath;

import java.util.Map;

import com.unifina.domain.data.Stream;
import com.unifina.service.FeedService;

// This class can also receive Strings for backwards compatibility 
public class StreamParameter extends Parameter<Stream> {
	
	public StreamParameter(AbstractSignalPathModule owner, String name) {
		super(owner,name,null,"Stream");
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
		}
		return config;
	}
	
	@Override
	Stream parseValue(String s) {
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
	
}
