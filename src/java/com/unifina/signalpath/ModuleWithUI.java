package com.unifina.signalpath;

import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.domain.data.Stream;
import com.unifina.service.PermissionService;
import com.unifina.service.StreamService;
import com.unifina.utils.IdGenerator;
import com.unifina.utils.MapTraversal;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ModuleWithUI extends AbstractSignalPathModule {

	protected String uiChannelId;
	protected boolean resendAll = false;
	protected int resendLast = 0;

	private transient Stream stream;
	private transient StreamService streamService;

	public ModuleWithUI() {
		super();
	}

	@Override
	public void initialize() {
		super.initialize();

		if (getGlobals().isRunContext()) {
			streamService = getGlobals().getGrailsApplication().getMainContext().getBean(StreamService.class);
			getGlobals().getDataSource().addStartListener(new IStartListener() {
				@Override
				public void onStart() {
					setupUiChannelStream();
				}
			});
			getGlobals().getDataSource().addStopListener(new IStopListener() {
				@Override
				public void onStop() {
					cleanupUiChannelStream();
				}
			});
		}
	}

	private void setupUiChannelStream() {
		stream = getStreamService().getStream(uiChannelId);

		// If not found by id, try to find the Stream by path. This UI channel may be dynamically generated, and the id is not saved in the JSON.
		if (stream == null && getParentSignalPath() != null && !getParentSignalPath().isRoot()) {
			stream = getStreamService().getStreamByUiChannelPath(getRuntimePath());

			// The uiChannelId may be replaced by a stream loaded by path from the db
			if (stream != null) {
				uiChannelId = stream.getId();
			}
		}

		// Else create a new Stream object for this UI channel
		if (stream == null) {
			// Initialize a new UI channel Stream
			Map<String, Object> params = new LinkedHashMap<>();
			params.put("name", getUiChannelName());
			params.put("uiChannel", true);
			params.put("uiChannelPath", getRuntimePath());
			params.put("uiChannelCanvas", getTopParentSignalPath().getCanvas());
			stream = getStreamService().createStream(params, getGlobals().getUser(), uiChannelId);
		}

		// User must have write permission to related Canvas in order to write to the UI channel
		if (!getGlobals().getGrailsApplication().getMainContext().getBean(PermissionService.class).canWrite(getGlobals().getUser(), stream.getUiChannelCanvas())) {
			throw new AccessControlException(this.getName() + ": User " + getGlobals().getUser().getUsername() +
					" does not have write access to UI Channel Stream " + stream.getId());
		}
	}

	protected void cleanupUiChannelStream() {
		if (getGlobals().isAdhoc()) {
			getStreamService().deleteStreamsDelayed(Arrays.asList(new Stream[] {getUiChannelStream()}));
		}
	}

	private StreamService getStreamService() {
		if (streamService == null) {
			streamService = getGlobals().getGrailsApplication().getMainContext().getBean(StreamService.class);
		}
		return streamService;
	}

	public Stream getUiChannelStream() {
		if (stream == null) {
			setupUiChannelStream();
		}
		return stream;
	}

	public void pushToUiChannel(Map msg) {
		if (stream == null) {
			setupUiChannelStream();
		}
		getStreamService().sendMessage(getUiChannelStream(), msg);
	}

	public String getUiChannelId() {
		return uiChannelId;
	}

	public void setUiChannelId(String uiChannelId) {
		this.uiChannelId = uiChannelId;
	}

	public String getUiChannelName() {
		return getEffectiveName();
	}

	public Map getUiChannelMap() {
		Map<String, String> uiChannel = new HashMap<>();
		uiChannel.put("id", getUiChannelId());
		uiChannel.put("name", getUiChannelName());
		uiChannel.put("webcomponent", getWebcomponentName());
		return uiChannel;
	}

	/**
	 * Override this method if a webcomponent is available for this module. The
	 * default implementation returns null, which means there is no webcomponent.
	 * @return The name of the webcomponent.
	 */
	public String getWebcomponentName() {
		if (domainObject == null) {
			return null;
		} else {
			return domainObject.getWebcomponent();
		}
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		config.put("uiChannel", getUiChannelMap());
		
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("uiResendAll", resendAll, "boolean"));
		options.add(new ModuleOption("uiResendLast", resendLast, "int"));
		
		return config;
	}
	
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// A Stream object will be created or loaded on start using the uiChannelId
		uiChannelId = MapTraversal.getString(config, "uiChannel.id");
		if (uiChannelId == null) {
			uiChannelId = IdGenerator.get();
		}
		
		ModuleOptions options = ModuleOptions.get(config);
		if (options.getOption("uiResendAll")!=null) {
			resendAll = options.getOption("uiResendAll").getBoolean();
		}
		if (options.getOption("uiResendLast")!=null) {
			resendLast = options.getOption("uiResendLast").getInt();
		}
		
	}

}
