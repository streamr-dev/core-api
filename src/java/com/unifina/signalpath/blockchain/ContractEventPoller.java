package com.unifina.signalpath.blockchain;

import com.unifina.domain.signalpath.Canvas;
import com.unifina.signalpath.AbstractSignalPathModule;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Poll Ethereum events of a contract via JSON RPC (https://github.com/ethereum/wiki/wiki/JSON-RPC) using filters.
 */
class ContractEventPoller implements Closeable, Runnable {
	private static final Logger log = Logger.getLogger(ContractEventPoller.class);
	private static final int POLL_INTERVAL_IN_MS = 3000;

	private final EthereumJsonRpc rpc;
	private final String contractAddress;
	private final Listener listener;
	private String filterId;

	interface Listener {
		void onEvent(JSONArray events);
		void onError(String message);
	}

	ContractEventPoller(String rpcUrl, String contractAddress, Listener listener) {
		this.rpc = new EthereumJsonRpc(rpcUrl);
		this.contractAddress = contractAddress;
		this.listener = listener;
	}

	@Override
	public void run() {
		newFilter();
		while (filterId != null) {
			pollChanges();
			try {
				Thread.sleep(POLL_INTERVAL_IN_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() {
		if (filterId != null) {
			uninstallFilter();
		}
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_newfilter
	 */
	private void newFilter() {
		List params = singletonList(singletonMap("address", contractAddress));
		try {
			filterId = rpc.rpcCall("eth_newFilter", params).getString("result");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		// Logging
		String id = null;
		if (listener instanceof AbstractSignalPathModule) {
			Canvas canvas = ((AbstractSignalPathModule) listener).getParentSignalPath().getCanvas();
			if (canvas != null) {
				id = canvas.getId();
			}
		}
		log.info(String.format("Filter '%s' created. Listening to contract '%s' on canvas '%s'.",
			filterId, contractAddress, id));
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getfilterchanges
	 */
	private synchronized void pollChanges() {
		log.info(String.format("Polling filter '%s'.", filterId));
		try {
			JSONObject response = rpc.rpcCall("eth_getFilterChanges", singletonList(filterId));
			JSONArray jsonArray = response.getJSONArray("result");
			if (jsonArray.length() != 0) {
				listener.onEvent(jsonArray);
			}
		} catch (EthereumJsonRpc.ErrorObjectError e) {
			if (filterDoesNotExist(e.getCode())) {
				log.info("Resetting filter...");
				filterId = null;
				newFilter();
			} else {
				listener.onError(e.getMessage());
			}
		} catch (EthereumJsonRpc.Error | JSONException e) {
			listener.onError(e.getMessage());
		}
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_uninstallfilter
	 */
	private void uninstallFilter() {
		boolean result;

		try {
			result = rpc.rpcCall("eth_uninstallFilter", singletonList(filterId)).getBoolean("result");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		if (result) {
			log.info(String.format("Filter '%s' uninstalled.", filterId));
			filterId = null;
		} else {
			listener.onError("Unable to uninstall filter " + filterId);
			throw new RuntimeException("Unable to uninstall filter " + filterId);
		}
	}

	private static boolean filterDoesNotExist(int code) {
		return code == -32000;
	}
}
