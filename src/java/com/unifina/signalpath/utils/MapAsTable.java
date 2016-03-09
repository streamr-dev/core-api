package com.unifina.signalpath.utils;

import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.ModuleWithUI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAsTable extends ModuleWithUI {
	private MapInput map = new MapInput(this, "map");
	private boolean headerSent = false;

	public MapAsTable() {
		super();
		resendAll = false;
		resendLast = 20;
	}

	@Override
	public void sendOutput() {
		if (!headerSent) {
			sendHeader();
			headerSent = true;
		}

		sendMessages(map.getValue());
	}

	@Override
	public void clearState() {
		headerSent = false;
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-table";
	}

	private void sendHeader() {
		Map<String, List<String>> headerDef = new HashMap<>();
		headerDef.put("headers", Arrays.asList("key", "value"));

		Map<String, Map<String, List<String>>> message = new HashMap<>();
		message.put("hdr", headerDef);
		pushToUiChannel(message);
	}

	private void sendMessages(Map<String, Object> value) {
		Map<String, Map<String, Object>> message = new HashMap<>();
		message.put("nm", value);
		pushToUiChannel(message);
	}
}
