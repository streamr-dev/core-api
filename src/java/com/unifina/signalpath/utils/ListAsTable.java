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

	public ListAsTable() {
		super();
		canClearState = false;
		resendAll = false;
		resendLast = 1;
	}

	@Override
	public void sendOutput() {
		List input = in.getValue();
		if (input == null || input.size() < 1) {
			Map headerDef = new HashMap<>();
			headerDef.put("headers", Arrays.asList("List is empty"));
			Map headerMessage = new HashMap<>();
			headerMessage.put("hdr", headerDef);
			pushToUiChannel(headerMessage);
			Map dataMessage = new HashMap<>();
			dataMessage.put("nc", Arrays.asList(new ArrayList<>()));
			pushToUiChannel(dataMessage);
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
		Map headerDef = new HashMap<>();
		headerDef.put("headers", headers);
		Map headerMessage = new HashMap<>();
		headerMessage.put("hdr", headerDef);
		pushToUiChannel(headerMessage);

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
		Map dataMessage = new HashMap<>();
		dataMessage.put("nc", contents);
		pushToUiChannel(dataMessage);
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-table";
	}

	@Override
	public void clearState() { }

	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		// We need to support unauthenticated initRequests for public views, so no authentication check
		if (request.getType().equals("initRequest")) {
			Map headerDef = new HashMap<>();
			headerDef.put("headers", Arrays.asList("index", "value"));
			Map headerMessage = new HashMap<>();
			headerMessage.put("hdr", headerDef);
			response.put("initRequest", headerMessage);
			response.setSuccess(true);
		} else {
			super.handleRequest(request, response);
		}
	}
}
