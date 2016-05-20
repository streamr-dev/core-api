package com.unifina.signalpath.kafka;

import com.unifina.service.PermissionService;
import grails.converters.JSON;

import java.security.AccessControlException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.service.KafkaService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.NotificationMessage;
import com.unifina.signalpath.Parameter;
import com.unifina.signalpath.StreamParameter;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.TimeSeriesInput;

public class SendToStream extends AbstractSignalPathModule {

	protected StreamParameter streamParameter = new StreamParameter(this,"stream");
	transient protected JSONObject streamConfig = null;
	
	transient protected KafkaService kafkaService = null;
	transient protected PermissionService permissionService = null;
	
	protected boolean historicalWarningShown = false;
	private Stream authenticatedStream = null;
	
	@Override
	public void init() {
		kafkaService = (KafkaService) globals.getGrailsApplication().getMainContext().getBean("kafkaService");
		permissionService = (PermissionService) globals.getGrailsApplication().getMainContext().getBean("permissionService");
		
		addInput(streamParameter);
		

		streamParameter.setUpdateOnChange(true);
		
		// TODO: don't rely on static ids
		Feed feedFilter = new Feed();
		feedFilter.setId(7L);
		streamParameter.setFeedFilter(feedFilter);
	}

	@Override
	public void sendOutput() {
		if (globals.isRealtime()) {
			Map msg = new LinkedHashMap<>();
			for (Input i : drivingInputs) {
				msg.put(i.getName(), i.getValue());
			}
			if (kafkaService == null) {
				kafkaService = (KafkaService) globals.getGrailsApplication().getMainContext().getBean("kafkaService");
			}
			kafkaService.sendMessage(authenticatedStream, "", msg);
		}
		else if (!historicalWarningShown && globals.getUiChannel()!=null) {
			globals.getUiChannel().push(new NotificationMessage(this.getName()+": Not sending to Stream '"+streamParameter.getValue()+"' in historical playback mode."), parentSignalPath.getUiChannelId());
			historicalWarningShown = true;
		}
	}

	@Override
	public void clearState() {}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		Stream stream = streamParameter.getValue();
		
		if (stream==null)
			return;
		
		// Check access to this Stream
		if (permissionService.canWrite(globals.getUser(), stream)) {
			authenticatedStream = stream;
		} else {
			throw new AccessControlException(this.getName()+": Access denied to Stream "+stream.getName());
		}
		
		// TODO: don't rely on static ids
		if (stream.getFeed().getId()!=7) {
			throw new IllegalArgumentException("Can not send to this feed type!");
		}
		
		if (stream.getConfig()==null) {
			throw new IllegalStateException("Stream " + stream.getName() + " is not properly configured!");
		}
		
		streamConfig = (JSONObject) JSON.parse(stream.getConfig());

		JSONArray fields = streamConfig.getJSONArray("fields");
		
		for (Object o : fields) {
			Input input = null;
			JSONObject j = (JSONObject) o;
			String type = j.getString("type");
			String name = j.getString("name");
			
			// TODO: add other types
			if (type.equalsIgnoreCase("number") || type.equalsIgnoreCase("boolean")) {
				input = new TimeSeriesInput(this, name);
				((TimeSeriesInput) input).canHaveInitialValue = false;
			} else if (type.equalsIgnoreCase("string")) {
				input = new StringInput(this, name);
			} else if (type.equalsIgnoreCase("map")) {
				input = new MapInput(this, name);
			} else if (type.equalsIgnoreCase("list")) {
				input = new ListInput(this, name);
			}

			if (input != null) {
				input.canToggleDrivingInput = false;
				input.canBeFeedback = false;
				input.requiresConnection = false;
				addInput(input);
			}
		}
		
		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}
	
}
