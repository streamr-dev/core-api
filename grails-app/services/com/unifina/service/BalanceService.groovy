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
import grails.util.Holders;
import javax.annotation.PostConstruct
import java.util.concurrent.ExecutionException
import com.unifina.utils.MapTraversal;

class BalanceService {
	protected EthereumModuleOptions ethereumOptions;
	protected Web3j web3j;
	@PostConstruct
	void init() {
		ethereumOptions = new EthereumModuleOptions()
		final HttpService httpService = new HttpService(ethereumOptions.getRpcUrl())
		web3j = Web3j.build(httpService)
	}

	String getDatacoinAddress(){
		String address = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.datacoinAddress");
		if (address == null) {
			throw new RuntimeException("No datacoin address found in config");
		}
		return address;
	}

	Map<String, BigInteger> getDatacoinBalances(SecUser user) throws InterruptedException, ExecutionException, MessageDecodingException {
		String datacoinAddress = getDatacoinAddress()
		def keys =
			IntegrationKey.findAllByUserAndServiceInList(user, [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID]);
		Map<String, BigInteger> rslt = new LinkedHashMap<String, BigInteger>();
		for (IntegrationKey ik : keys) {
			rslt.put(ik.idInService, Web3jHelper.getERC20Balance(web3j, datacoinAddress, ik.idInService))
		}
		return rslt;
	}

	@CompileStatic
	Map<String, BigInteger> checkBalances(SecUser user) throws ApiException {
		try {
			return getDatacoinBalances(user)
		} catch (ExecutionException e) {
			throw new ApiException(500, "BALANCE_ERROR", e.getCause().getMessage())
		} catch (MessageDecodingException e) {
			throw new ApiException(500, "BALANCE_ERROR", e.getCause().getMessage())
		} catch (InterruptedException e) {
			throw new ApiException(500, "BALANCE_DECODING_ERROR", e.getMessage())
		}
	}
}

