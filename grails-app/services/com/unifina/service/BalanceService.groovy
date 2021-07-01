package com.unifina.service

import com.unifina.domain.IntegrationKey
import com.unifina.domain.User
import com.unifina.utils.ApplicationConfig
import com.unifina.utils.EthereumSettings
import org.web3j.exceptions.MessageDecodingException
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

import javax.annotation.PostConstruct
import java.util.concurrent.ExecutionException

class BalanceService {
	Web3jHelperService web3jHelperService

	protected EthereumSettings ethereumSettings;
	protected web3j;

	private String dataCoinAddress;

	@PostConstruct
	void init() {
		ethereumSettings = new EthereumSettings()
		final HttpService httpService = new HttpService(ethereumSettings.getRpcUrl())
		web3j = Web3j.build(httpService)
		dataCoinAddress = ApplicationConfig.getString("streamr.ethereum.datacoinAddress");
		if (dataCoinAddress == null) {
			throw new RuntimeException("No datacoin address found in config");
		}
	}

	Map<String, BigInteger> getDatacoinBalances(User user) throws InterruptedException, ExecutionException, MessageDecodingException {
		def keys =
			IntegrationKey.findAllByUserAndServiceInList(user, [IntegrationKey.Service.ETHEREUM_ID]);

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

