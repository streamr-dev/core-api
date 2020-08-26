package com.unifina.signalpath.blockchain;

import com.unifina.domain.Canvas;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.utils.ThreadUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Closeable;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Poll Ethereum events of a contract via JSON RPC (https://github.com/ethereum/wiki/wiki/JSON-RPC) using filters.
 */
class ContractEventPoller implements Closeable, Runnable, JsonRpcResponseHandler {
	private static final Logger log = Logger.getLogger(ContractEventPoller.class);
	private static final int POLL_INTERVAL_IN_MS = 3000;
	public static final int RPC_CODE_NO_FILTER = -32000;

	private static final int ID_ADDFILTER = 1;
	private static final int ID_REMOVEFILTER = 2;
	private static final int ID_POLLFILTER = 3;

	private final EthereumJsonRpc rpc;
	private final String contractAddress;
	private final EventsListener listener;
	private String filterId;
	//keepPolling starts true, and only turns false when close() is called
	private boolean keepPolling = true;


	ContractEventPoller(String rpcUrl, String contractAddress, EventsListener listener) {
		this.contractAddress = contractAddress;
		this.listener = listener;

		if (rpcUrl.startsWith("http")) {
			rpc = new HttpEthereumJsonRpc(rpcUrl, this);
		} else {
			rpc = new WebsocketEthereumJsonRpc(rpcUrl, this);
			boolean opened = ((WebsocketEthereumJsonRpc) rpc).openConnectionRetryIfFail();
			if (!opened) {
				throw new RuntimeException("Couldnt open connection to " + rpcUrl);
			}
		}
	}

	@Override
	public void init(){
		newFilter();
	}

	@Override
	public void run() {
		while (keepPolling) {
			pollChanges();
			ThreadUtil.sleep(POLL_INTERVAL_IN_MS);
		}
	}

	@Override
	public void close() {
		if (filterId != null) {
			uninstallFilter();
		}
		keepPolling = false;
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_newfilter
	 */
	private void newFilter() {
		List params = singletonList(singletonMap("address", contractAddress));
		try {
			rpc.rpcCall("eth_newFilter", params, ID_ADDFILTER);
			log.info("adding new filter to contract address " + contractAddress);
		} catch (Exception e) {
			listener.onError(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	protected void processAddFilterResponse(JSONObject resp) {
		filterId = resp.getString("result");
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

	private synchronized void pollChanges() {
		if (filterId == null) {
			log.info("pollChanges called before filter is set. Doing nothing.");
			return;
		}
		try {
			log.debug(String.format("Polling filter '%s'.", filterId));
			rpc.rpcCall("eth_getFilterChanges", singletonList(filterId), ID_POLLFILTER);
		}  catch (Exception e) {
			listener.onError(e.getMessage());
			log.error("pollChanges threw exception. This might be normal if websocket connection is reopening. Error: " + e.getMessage());
		}
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getfilterchanges
	 */
	private synchronized void processPollChangesResponse(JSONObject response) {
		JSONArray jsonArray = response.getJSONArray("result");
		if (jsonArray.length() != 0) {
			listener.onEvent(jsonArray);
		}
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_uninstallfilter
	 */
	private void uninstallFilter() {
		boolean result;

		// avoid race condition where another close() enters while rpcCall is executing
		String id = filterId;
		filterId = null;

		try {
			rpc.rpcCall("eth_uninstallFilter", singletonList(id), ID_REMOVEFILTER);
		} catch (Exception e) {
			listener.onError(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void processUninstallFilterResponse(JSONObject response) {
		boolean result;

		// avoid race condition where another close() enters while rpcCall is executing
		String id = filterId;
		filterId = null;
		result = response.getBoolean("result");
		if (result) {
			log.info(String.format("Filter '%s' uninstalled.", id));
		} else {
			log.error("Unable to uninstall filter " + id);
			listener.onError("Unable to uninstall filter " + id);
		}
	}

	/*
		handles both error and non-error RPC json resposnes
	 */
	@Override
	public void processResponse(JSONObject resp) {
		if(resp.has("error")){
			log.error("RPC err: "+ resp);
			JSONObject err = resp.getJSONObject("error");
			if (RPC_CODE_NO_FILTER == err.getInt("code")){
				log.warn("Seen missing filter error. Resetting filter.");
				newFilter();
			}
			return;
		}

		int id = resp.getInt("id");
		switch (id) {
			case ID_ADDFILTER:
				processAddFilterResponse(resp);
				return;
			case ID_REMOVEFILTER:
				processUninstallFilterResponse(resp);
				return;
			case ID_POLLFILTER:
				processPollChangesResponse(resp);
				return;
			default:
				throw new RuntimeException("Unknown RPC id " + id);
		}
	}
}
