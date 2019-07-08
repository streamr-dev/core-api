package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.json.JSONObject;

import java.util.List;

public abstract class EthereumJsonRpc {
	protected final String url;
	protected JsonRpcResponseHandler handler;
	EthereumJsonRpc(String url, JsonRpcResponseHandler handler) {
		this.url = url;
		this.handler = handler;
	}

	/*
		implementation should call handler.processResponse()
	 */
	public abstract void rpcCall(String method, List params, int callId) throws Exception;

	protected String formRequestBody(String method, List params, int callId) {
		return new Gson().toJson(ImmutableMap.of(
				"id", callId,
				"jsonrpc", "2.0",
				"method", method,
				"params", params
			));
	}


}
