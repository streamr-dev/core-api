package com.unifina.signalpath;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.Permission;
import com.unifina.domain.security.SecUser;
import com.unifina.domain.signalpath.Module;
import com.unifina.service.PermissionService;
import com.unifina.service.StreamService;
import com.unifina.signalpath.utils.MessageChainUtil;
import com.unifina.utils.IdGenerator;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ModuleWithUI extends AbstractSignalPathModule {

	private UiChannel uiChannel;
	protected int resendLast = 0;

	private transient StreamService streamService;
	transient private com.streamr.client.rest.Stream cachedStream = null;

	private static final Logger log = Logger.getLogger(ModuleWithUI.class);

	public ModuleWithUI() {
		super();
	}

	@Override
	public void initialize() {
		super.initialize();

		if (getGlobals().isRunContext()) {
			getGlobals().getDataSource().addStartListener(ModuleWithUI.this::onStart);
		}
	}

	protected void onStart() {
		getUiChannel().initialize();
	}

	private StreamService getStreamService() {
		if (streamService == null) {
			streamService = Holders.getApplicationContext().getBean(StreamService.class);
		}
		return streamService;
	}

	public void pushToUiChannel(Map<String, Object> content) {
		Stream stream = getUiChannel().getStream();

		try {
			com.streamr.client.rest.Stream s = cacheStream(stream);
			getGlobals().getStreamrClient().publish(s, content, getGlobals().getTime());
		} catch (Exception e) {
			log.error("Failed to publish to UI channel: ", e);
		}
	}

	public UiChannel getUiChannel() {
		if (uiChannel == null) {
			throw new RuntimeException("Module has not been configured!");
		}
		return uiChannel;
	}

	public String getUiChannelName() {
		return getUiChannel().getName();
	}
	/**
	 * Override this method if a webcomponent is available for this module. The
	 * default implementation returns null, which means there is no webcomponent.
	 * @return The name of the webcomponent.
	 */
	public String getWebcomponentName() {
		Module domainObject = getDomainObject();
		if (domainObject == null) {
			return null;
		} else {
			return domainObject.getWebcomponent();
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		if (uiChannel != null) {
			config.put("uiChannel", uiChannel.toMap());
		}

		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("uiResendLast", resendLast, "int"));

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// A Stream object will be created or loaded on start using the uiChannelId
		String uiChannelId = MapTraversal.getString(config, "uiChannel.id");
		uiChannel = new UiChannel(
				uiChannelId == null ? IdGenerator.getShort() : uiChannelId,
				getEffectiveName(),
				uiChannelId == null);

		ModuleOptions options = ModuleOptions.get(config);
		if (options.getOption("uiResendLast")!=null) {
			resendLast = options.getOption("uiResendLast").getInt();
		}

	}

	public void ensureUiChannel() {
		uiChannel.initialize();
	}

	private com.streamr.client.rest.Stream cacheStream(Stream stream) throws IOException {
		if (cachedStream == null || !stream.getId().equals(cachedStream.getId()) ) {
			cachedStream = getGlobals().getStreamrClient().getStream(stream.getId());
		}
		return cachedStream;
	}

	public class UiChannel implements Serializable {

		private String id;
		private final String name;
		private boolean isNew;

		private transient Stream stream; // initialized lazily by calling initialize()

		public UiChannel(String id, String name, boolean isNew) {
			this.id = id;
			this.name = name;
			this.isNew = isNew;
		}

		public String getId() {
			return id;
		}

		public Stream getStream() {
			if (!isInitialized()) {
				initialize();
			}
			return stream;
		}

		public String getName() {
			return name;
		}

		/**
		 * The Stream objects for UI channels are created lazily on Canvas start.
		 * There is always an uiChannelId, but the Stream object will not pre-exist when
		 * the Canvas is started for the first time. In that case, it needs to be created.
		 * The Stream object is found either by id directly or by uiChannelPath in the
		 * case that it's created dynamically and not saved in the JSON.
		 */
		public void initialize() {
			// Avoid lookup if we are sure the Stream won't be found
			if (!isNew) {
				stream = getStreamService().getStream(id);
			}

			// If not found by id, try to find the Stream by path. This UI channel may be dynamically generated, and the id is not saved in the JSON.
			if (stream == null) {
				stream = getStreamService().getStreamByUiChannelPath(getRuntimePath());

				// The uiChannelId may be replaced by a stream loaded by path from the db
				if (stream != null) {
					id = stream.getId();
				}
			}

			SecUser user = SecUser.getViaJava(getGlobals().getUserId());

			// Else create a new Stream object for this UI channel
			if (stream == null) {
				// Initialize a new UI channel Stream
				Map<String, Object> params = new LinkedHashMap<>();
				params.put("name", getUiChannelName());
				params.put("uiChannel", true);
				params.put("uiChannelPath", getRuntimePath());
				params.put("uiChannelCanvas", getRootSignalPath().getCanvas());
				log.warn("uiChannel stream " + id + " was not found. Creating a new stream with params: "+params);
				stream = getStreamService().createStream(params, user, id);
			}

			// Fix for CORE-893: Guard against excessive memory use by setting stream.uiChannelCanvas to the instance already in memory
			stream.setUiChannelCanvas(getRootSignalPath().getCanvas());

			// User must have write permission to related Canvas in order to write to the UI channel
			PermissionService permissionService = Holders.getApplicationContext().getBean(PermissionService.class);
			if (!permissionService.check(user, stream, Permission.Operation.STREAM_PUBLISH)) {
				throw new AccessControlException(ModuleWithUI.this.getName() + ": User " + user.getUsername() +
						" does not have write access to UI Channel Stream " + stream.getId());
			}

			isNew = false;
		}

		public boolean isInitialized() {
			return stream != null;
		}

		public Map toMap() {
			Map<String, String> map = new HashMap<>();
			map.put("id", getId());
			map.put("name", getUiChannelName());
			map.put("webcomponent", getWebcomponentName());
			return map;
		}
	}

}
