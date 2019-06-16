package com.unifina.signalpath.blockchain;

import org.json.JSONArray;

public interface EventsListener {
	void onEvent(JSONArray events);
	void onError(String message);
}
