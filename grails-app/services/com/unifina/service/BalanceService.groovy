package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.blockchain.EthereumModuleOptions
import com.unifina.signalpath.blockchain.Web3jHelper
import groovy.transform.CompileStatic
import org.web3j.exceptions.MessageDecodingException
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.http.HttpService

import javax.annotation.PostConstruct
import java.util.concurrent.ExecutionException

class BalanceService {
	protected Web3Balance web3
	protected EthereumModuleOptions ethereumOptions;
	@PostConstruct
	void init() {
		ethereumOptions = new EthereumModuleOptions()
		final HttpService httpService = new HttpService(ethereumOptions.getRpcUrl())
		final Web3j web3j = Web3j.build(httpService)
		this.web3 = new Web3BalanceImpl(web3j,ethereumOptions.getDatacoinAddress())
	}

	@CompileStatic
	Map<String, BigInteger> checkBalances(SecUser user) throws ApiException {
		try {
			return this.web3.getDatacoinBalances(user)
		} catch (ExecutionException e) {
			throw new ApiException(500, "BALANCE_ERROR", e.getCause().getMessage())
		} catch (MessageDecodingException e) {
			throw new ApiException(500, "BALANCE_ERROR", e.getCause().getMessage())
		} catch (InterruptedException e) {
			throw new ApiException(500, "BALANCE_DECODING_ERROR", e.getMessage())
		}
	}
}

abstract class Web3Balance {
	abstract BigInteger getDatacoinBalance(String address) throws InterruptedException, ExecutionException, MessageDecodingException

	Map<String, BigInteger> getDatacoinBalances(SecUser user) throws InterruptedException, ExecutionException, MessageDecodingException  {
		def keys =
			IntegrationKey.findAllByUserAndService(user, IntegrationKey.Service.ETHEREUM | IntegrationKey.Service.ETHEREUM_ID);
		Map<String, BigInteger> rslt = new LinkedHashMap<String, BigInteger>();
		for(IntegrationKey ik : keys){
			rslt.put(ik.idInService, getDatacoinBalance(ik.idInService))
		}
		return rslt;
	}
}

class Web3BalanceImpl extends Web3Balance {
	private Web3j web3
	private String datacoinAddress

	Web3BalanceImpl(Web3j web3, String datacoinAddress) {
		this.web3 = web3
		this.datacoinAddress = datacoinAddress
	}

	@Override
	BigInteger getDatacoinBalance(String holderAddress) throws InterruptedException, ExecutionException, MessageDecodingException {
		return Web3jHelper.getERC20Balance(web3, datacoinAddress, holderAddress)
	}
}
