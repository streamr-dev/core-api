package com.unifina.signalpath.blockchain;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.Closeable;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Poll Ethereum events of a contract via JSON RPC (https://github.com/ethereum/wiki/wiki/JSON-RPC) using filters.
 */
class ContractEventPoller implements Closeable {
	private static final Logger log = Logger.getLogger(ContractEventPoller.class);

	private static final int SOME_CALL_ID = 123;

	private final EthereumJsonRpc rpc;
	private final String contractAddress;
	private String filterId;

	ContractEventPoller(String rpcUrl, String contractAddress) {
		this.rpc = new EthereumJsonRpc(rpcUrl);
		this.contractAddress = contractAddress;
		newFilter();
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getfilterchanges
	 */
	JSONArray poll(Integer callId) throws JSONException {
		if (filterId == null) {
			throw new RuntimeException("Filter not installed. Perhaps poller has been closed already?");
		}

		log.info(String.format("Polling filter '%s'.", filterId));
		try {
			return rpc.rpcCall("eth_getFilterChanges", singletonList(filterId), callId)
				.getJSONArray("result");
		} catch (EthereumJsonRpc.ErrorObjectError e) {
			if (e.getCode() == -32000) { // TODO: this code is not documented, might change?
				filterId = null;
				newFilter();
				return null;
			}
			throw e;
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
			filterId = rpc.rpcCall("eth_newFilter", params, SOME_CALL_ID).getString("result");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		log.info(String.format("Filter '%s' created.", filterId));
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_uninstallfilter
	 */
	private void uninstallFilter() {
		boolean result;

		try {
			result = rpc.rpcCall("eth_uninstallFilter", singletonList(filterId), SOME_CALL_ID)
				.getBoolean("result");
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		if (result) {
			log.info(String.format("Filter '%s' uninstalled.", filterId));
			filterId = null;
		} else {
			throw new RuntimeException("Unable to install filter " + filterId);
		}
	}
}
