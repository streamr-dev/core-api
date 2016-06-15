package com.unifina.signalpath.utils;

import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.RuntimeRequest;
import com.unifina.signalpath.RuntimeResponse;

import java.util.*;

/**
 * Render list as table, and if list items are maps, split them to columns
 * This can be used to e.g. nicely show SQL results
 */
public class ListAsTable extends ModuleWithUI {
	private ListInput in = new ListInput(this, "list");

	private static final List<String> emptyHeaders = Arrays.asList("List is empty");
	private List<String> currentHeaders;

	public ListAsTable() {
		super();
		resendAll = false;
		resendLast = 1;
	}

	/** @returns true if headers were actually sent (changed from what is shown in client) */
	private boolean sendHeaders(List<String> headers) {
		if (headers.equals(currentHeaders)) { return false; }
		pushToUiChannel(buildHeaderMessage(headers));
		currentHeaders = headers;
		return true;
	}

	private void sendBody(List<List<Object>> contents) {
		Map dataMessage = new HashMap<>();
		dataMessage.put("nc", contents);
		pushToUiChannel(dataMessage);
	}

	private Map buildHeaderMessage(List<String> headers) {
		if (headers == null) { headers = emptyHeaders; }
		Map headerDef = new HashMap<>();
		headerDef.put("headers", headers);
		Map headerMessage = new HashMap<>();
		headerMessage.put("hdr", headerDef);
		return headerMessage;
	}

	@Override
	public void sendOutput() {
		List input = in.getValue();
		if (input == null || input.size() < 1) {
			if (sendHeaders(emptyHeaders)) {
				// headers were non-emptyHeaders, so content should be reset too
				sendBody(Arrays.asList((List<Object>) new ArrayList<>()));
			}
			return;
		}

		// headers: index i plus all distinct keys in all maps, additionally value column for non-maps
		Set headerSet = new LinkedHashSet<>();
		boolean hasValueColumn = false;
		for (Object item : input) {
			if (item instanceof Map) {
				Map object = (Map)item;
				headerSet.addAll(object.keySet());
			} else if (item != null) {
				hasValueColumn = true;
			}
		}
		List<String> headers = new ArrayList<>();
		headers.add("i");
		if (hasValueColumn) { headers.add("value"); }
		headers.addAll(headerSet);
		if (!headers.equals(currentHeaders)) {
			sendHeaders(headers);
		}

		List<List<Object>> contents = new ArrayList<>();
		for (int i = 0; i < input.size(); i++) {
			List<Object> row = new ArrayList<>();
			row.add(i);        // index
			Object item = input.get(i);
			if (item instanceof Map) {
				if (hasValueColumn) { row.add(""); }
				Map object = (Map)item;
				for (int j = row.size(); j < headers.size(); j++) {
					Object value = object.get(headers.get(j));
					row.add(value != null ? value : "");
				}
			} else {
				row.add(item);
			}
			contents.add(row);
		}
		sendBody(contents);
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-table";
	}

	@Override
	public void clearState() {
		currentHeaders = null;
	}

	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		// We need to support unauthenticated initRequests for public views, so no authentication check
		if (request.getType().equals("initRequest")) {
			response.put("initRequest", buildHeaderMessage(currentHeaders));
			response.setSuccess(true);
		} else {
			super.handleRequest(request, response);
		}
	}
}
