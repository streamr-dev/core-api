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
		handler.init();
	}

	@Override
	public void rpcCall(String method, List params, int callId) throws HttpStatusException, ErrorObjectException, com.mashape.unirest.http.exceptions.UnirestException {
			HttpResponse<JsonNode> response = formRequest(method, params, callId).asJson();

			if (statusCodeIsNot2XX(response.getStatus())) {
				throw new HttpStatusException(response.getStatus(), response.getBody());
			}

			JSONObject responseJson = response.getBody().getObject();
			if (responseJson.opt("error") != null) {
				throw new ErrorObjectException(responseJson.optJSONObject("error"));
			}
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

	abstract class RPCException extends Exception {
		private RPCException(String message) {
			super(String.format("JSON RPC error with server '%s': %s", url, message));
		}
	}


	class HttpStatusException extends RPCException {
		private HttpStatusException(int statusCode, JsonNode body) {
			super(String.format("unexpected status code %d with content %s", statusCode, body));
		}
	}

	class ErrorObjectException extends RPCException {
		private final JSONObject errorObject;

		private ErrorObjectException(JSONObject errorObject) {
			super("response contained error object " + errorObject);
			this.errorObject = errorObject;
		}

		public int getCode() {
			return errorObject.optInt("code");
		}
	}
}
