package com.unifina.service

import com.unifina.utils.Web3jHelper
import org.web3j.protocol.Web3j

import java.util.concurrent.ExecutionException

class Web3jHelperService {

	// wrap the static method for testing
	BigInteger getERC20Balance(Web3j web3j, String erc20address, String holderAddress) throws ExecutionException, InterruptedException {
		return Web3jHelper.getERC20Balance(web3j, erc20address, holderAddress)
	}
}
