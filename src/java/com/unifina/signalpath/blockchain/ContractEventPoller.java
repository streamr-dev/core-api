package com.unifina.signalpath.blockchain;

import com.unifina.domain.signalpath.Canvas;
import com.unifina.signalpath.AbstractSignalPathModule;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.websocket.DeploymentException;
import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Poll Ethereum events of a contract via JSON RPC (https://github.com/ethereum/wiki/wiki/JSON-RPC) using filters.
 */
class ContractEventPoller implements Closeable, Runnable, JsonRpcResponseHandler {
	private static final Logger log = Logger.getLogger(ContractEventPoller.class);
	private static final int POLL_INTERVAL_IN_MS = 3000;

	private static final int ID_ADDFILTER = 1;
	private static final int ID_REMOVEFILTER = 2;
	private static final int ID_POLLFILTER = 3;

	private final EthereumJsonRpc rpc;
	private final String contractAddress;
	private final EventsListener listener;
	private String filterId;


	ContractEventPoller(String rpcUrl, String contractAddress, EventsListener listener) throws DeploymentException, IOException, URISyntaxException {
		this.rpc = new WebsocketEthereumJsonRpc(rpcUrl, this);
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
			rpc.rpcCall("eth_newFilter", params, ID_ADDFILTER);
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
		try {
			log.info(String.format("Polling filter '%s'.", filterId));
			rpc.rpcCall("eth_getFilterChanges", singletonList(filterId), ID_POLLFILTER);
		}
		catch (HttpEthereumJsonRpc.ErrorObjectException e) {
			if (filterDoesNotExist(e.getCode())) {
				log.info("Resetting filter...");
				filterId = null;
				newFilter();
			} else {
				listener.onError(e.getMessage());
			}
		} catch (HttpEthereumJsonRpc.RPCException | JSONException e) {
			listener.onError(e.getMessage());
		}
		catch(Exception e){
			listener.onError(e.getMessage());
			throw new RuntimeException(e);
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
		}
		catch(Exception e){
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
			listener.onError("Unable to uninstall filter " + id);
			throw new RuntimeException("Unable to uninstall filter " + id);
		}
	}

	private static boolean filterDoesNotExist(int code) {
		return code == -32000;
	}

	@Override
	public void processResponse(JSONObject resp) {
		int id = resp.getInt("id");
		switch(id){
			case ID_ADDFILTER: processAddFilterResponse(resp); return;
			case ID_POLLFILTER: processPollChangesResponse(resp); return;
			case ID_REMOVEFILTER: processUninstallFilterResponse(resp); return;
		}
		throw new RuntimeException("Unknown RPC id "+id);
	}
}
