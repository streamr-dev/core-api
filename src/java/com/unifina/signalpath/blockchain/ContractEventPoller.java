package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.Closeable;

import static java.util.Collections.singletonList;

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
			return rpc.ethGetFilterChanges(singletonList(filterId), callId);
		} catch (EthereumJsonRpc.Error e) {
			if (Integer.valueOf(-32000).equals(e.getCode())) {
				filterId = null; // TODO: should try to uninstall?
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
		try {
			filterId = rpc.ethNewFilter(singletonList(ImmutableMap.of("address", contractAddress)), SOME_CALL_ID);
			log.info(String.format("Filter '%s' created.", filterId));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_uninstallfilter
	 */
	private void uninstallFilter() {
		boolean result;

		try {
			result = rpc.ethUninstallFilter(singletonList(filterId), SOME_CALL_ID);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		if (result) {
			log.info(String.format("Filter '%s' uninstalled.", filterId));
			filterId = null;
		} else {
			throw new RuntimeException("JSON RPC server was unable to install filter " + filterId);
		}
	}
}
