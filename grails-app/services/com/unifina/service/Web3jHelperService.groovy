package com.unifina.service

import org.web3j.protocol.Web3j

import java.util.concurrent.ExecutionException

class Web3jHelperService {
	/**
	 * @param erc20address address of ERC20 or 0x0 for ETH balance
	 * @return token balance in wei
	 */
	BigInteger getERC20Balance(Web3j web3j, String erc20address, String holderAddress) throws ExecutionException, InterruptedException {
		return Web3jHelper.getERC20Balance(web3j, erc20address, holderAddress)
	}
}
