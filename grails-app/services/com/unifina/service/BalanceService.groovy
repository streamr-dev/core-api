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
	Web3jHelperService web3jHelperService

	protected EthereumModuleOptions ethereumOptions;
	protected web3j;

	private String dataCoinAddress;

	@PostConstruct
	void init() {
		ethereumOptions = new EthereumModuleOptions()
		final HttpService httpService = new HttpService(ethereumOptions.getRpcUrl())
		web3j = Web3j.build(httpService)
		dataCoinAddress = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.datacoinAddress");
		if (dataCoinAddress == null) {
			throw new RuntimeException("No datacoin address found in config");
		}
	}

	Map<String, BigInteger> getDatacoinBalances(SecUser user) throws InterruptedException, ExecutionException, MessageDecodingException {
		def keys =
			IntegrationKey.findAllByUserAndServiceInList(user, [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID]);

		Map<String, BigInteger> result = new LinkedHashMap<String, BigInteger>();
		for (IntegrationKey ik : keys) {
			try {
				result.put(ik.idInService, web3jHelperService.getERC20Balance(web3j, dataCoinAddress, ik.idInService))
			} catch (ExecutionException e) {
				throw new ApiException(500, "BALANCE_ERROR_EXECUTION_EXCEPTION", e.getMessage())
			} catch (RuntimeException e) {
				throw new ApiException(500, "BALANCE_ERROR_RUNTIME_EXCEPTION", e.getMessage())
			} catch (InterruptedException e) {
				throw new ApiException(500, "BALANCE_ERROR_INTERRUPTED_EXCEPTION", e.getMessage())
			}
		}
		return result;
	}
}

