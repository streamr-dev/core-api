package com.unifina.signalpath.blockchain;

import org.json.JSONObject;

/*
processes raw JSON response
 */

public interface JsonRpcResponseHandler {
	public void init();
	public void processResponse(JSONObject resp);
}
