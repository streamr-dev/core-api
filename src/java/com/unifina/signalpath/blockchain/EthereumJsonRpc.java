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

class EthereumJsonRpc {
	private static final int CALL_ID = 123;
	private final String url;

	EthereumJsonRpc(String url) {
		this.url = url;
	}

	JSONObject rpcCall(String method, List params) throws UnirestError, HttpStatusError, ErrorObjectError {
		try {
			HttpResponse<JsonNode> response = formRequest(method, params, CALL_ID).asJson();

			if (statusCodeIsNot2XX(response.getCode())) {
				throw new HttpStatusError(response.getCode(), response.getBody());
			}

			JSONObject responseJson = response.getBody().getObject();
			if (responseJson.opt("error") != null) {
				throw new ErrorObjectError(responseJson.optJSONObject("error"));
			}

			return responseJson;
		} catch (UnirestException e) {
			throw new UnirestError(e);
		}
	}

	private RequestBodyEntity formRequest(String method, List params, int callId) {
		return Unirest.post(url).body(new Gson().toJson(ImmutableMap.of(
			"id", callId,
			"jsonrpc", "2.0",
			"method", method,
			"params", params
		)));
	}

	private static boolean statusCodeIsNot2XX(int code) {
		return code / 100 != 2;
	}

	abstract class Error extends RuntimeException {
		private Error(String message) {
			super(String.format("JSON RPC error with server '%s': %s", url, message));
		}
	}

	class UnirestError extends Error {
		private UnirestError(UnirestException e) {
			super(e.getMessage());
		}
	}

	class HttpStatusError extends Error {
		private HttpStatusError(int statusCode, JsonNode body) {
			super(String.format("unexpected status code %d with content %s", statusCode, body));
		}
	}

	class ErrorObjectError extends Error {
		private final JSONObject errorObject;

		private ErrorObjectError(JSONObject errorObject) {
			super("response contained error object " + errorObject);
			this.errorObject = errorObject;
		}

		public int getCode() {
			return errorObject.optInt("code");
		}
	}
}
