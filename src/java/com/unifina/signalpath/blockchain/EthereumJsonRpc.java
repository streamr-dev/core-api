package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class EthereumJsonRpc {
	private final String url;

	EthereumJsonRpc(String url) {
		this.url = url;
	}

	// Convenience method
	JSONArray ethGetFilterChanges(List params, int callId) throws Error, JSONException {
		JSONObject response = rpcCall("eth_getFilterChanges", params, callId);
		return response.getJSONArray("result");
	}

	// Convenience method
	String ethNewFilter(List params, int callId) throws Error, JSONException {
		JSONObject response = rpcCall("eth_newFilter", params, callId);
		return response.getString("result");
	}

	// Convenience method
	boolean ethUninstallFilter(List params, int callId) throws Error, JSONException {
		JSONObject response = rpcCall("eth_uninstallFilter", params, callId);
		return response.getBoolean("result");
	}

	JSONObject rpcCall(String method, List params, int callId) throws Error {
		try {
			HttpResponse<JsonNode> response = Unirest.post(url)
				.body(formRequestBody(method, params, callId))
				.asJson();

			if (statusCodeIsNot2XX(response.getCode())) {
				throw new Error(response.getCode(), response.getBody());
			}

			JSONObject responseJson = response.getBody().getObject();
			if (responseJson.opt("error") != null) {
				throw new Error(responseJson.optJSONObject("error"));
			}

			return responseJson;
		} catch (UnirestException e) {
			throw new Error(e);
		}
	}

	private static String formRequestBody(String method, List params, int callId) {
		return new Gson().toJson(ImmutableMap.of(
			"id", callId,
			"jsonrpc", "2.0",
			"method", method,
			"params", params
		));
	}


	private static boolean statusCodeIsNot2XX(int code) {
		return code / 100 != 2;
	}

	class Error extends RuntimeException {
		private JSONObject errorObject;

		private Error(UnirestException e) {
			super(String.format("JSON RPC communication failure with server '%s'", url), e);
		}

		private Error(int statusCode, JsonNode body) {
			super(String.format("JSON RPC server '%s' returned unexpected status code %d with content %s",
				url, statusCode, body));
		}

		private Error(JSONObject errorObject) {
			super(String.format("JSON RPC server '%s' returned domain error '%s'", url, errorObject));
			this.errorObject = errorObject;
		}

		public String getErrorMessage() {
			return errorObject == null ? null : errorObject.optString("message");
		}

		public Integer getCode() {
			return errorObject == null ? null : errorObject.optInt("code");
		}
	}
}
