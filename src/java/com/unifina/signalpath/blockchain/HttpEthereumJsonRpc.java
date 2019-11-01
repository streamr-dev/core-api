package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.json.JSONObject;

import java.util.List;

class HttpEthereumJsonRpc extends EthereumJsonRpc{
	HttpEthereumJsonRpc(String url, JsonRpcResponseHandler handler) {
		super(url, handler);
		//in the case of ContractEventPoller, handler.init() installs filter
		handler.init();
	}

	@Override
	public void rpcCall(String method, List params, int callId) throws HttpStatusException, com.mashape.unirest.http.exceptions.UnirestException {
			HttpResponse<JsonNode> response = formRequest(method, params, callId).asJson();

			if (statusCodeIsNot2XX(response.getStatus())) {
				throw new HttpStatusException(response.getStatus(), response.getBody());
			}

			JSONObject responseJson = response.getBody().getObject();
			handler.processResponse(responseJson);
	}


	private RequestBodyEntity formRequest(String method, List params, int callId) {
		return Unirest. post(url)
				.header("Content-Type", "application/json")
				.body(formRequestBody(method, params, callId));
	}

	private static boolean statusCodeIsNot2XX(int code) {
		return code / 100 != 2;
	}

	class HttpStatusException extends RPCException {
		private HttpStatusException(int statusCode, JsonNode body) {
			super(String.format("unexpected status code %d with content %s", statusCode, body));
		}
	}
}
