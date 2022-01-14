package com.unifina.service

import com.unifina.domain.User
import com.unifina.utils.ApplicationConfig
import com.unifina.utils.EthereumConfig
import org.web3j.exceptions.MessageDecodingException
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

import javax.annotation.PostConstruct
import java.util.concurrent.ExecutionException

class BalanceService {
	Web3jHelperService web3jHelperService

	private EthereumConfig ethereumConf;
	private web3j;

	private String dataCoinAddress;

	@PostConstruct
	void init() {
		ethereumConf = new EthereumConfig(ApplicationConfig.getString("streamr.ethereum.defaultNetwork"))
		final HttpService httpService = new HttpService(ethereumConf.getRpcUrl())
		web3j = Web3j.build(httpService)
		dataCoinAddress = ApplicationConfig.getString("streamr.ethereum.datacoinAddress");
		if (dataCoinAddress == null) {
			throw new RuntimeException("No datacoin address found in config");
		}
	}

	Map<String, BigInteger> getDatacoinBalances(User user) throws InterruptedException, ExecutionException, MessageDecodingException {
		Map<String, BigInteger> result = new LinkedHashMap<String, BigInteger>()
		try {
			String address = user.getUsername()
			BigInteger balance = web3jHelperService.getERC20Balance(web3j, dataCoinAddress, address)
			result.put(address, balance)
		} catch (ExecutionException e) {
			throw new ApiException(500, "BALANCE_ERROR_EXECUTION_EXCEPTION", e.getMessage())
		} catch (RuntimeException e) {
			throw new ApiException(500, "BALANCE_ERROR_RUNTIME_EXCEPTION", e.getMessage())
		} catch (InterruptedException e) {
			throw new ApiException(500, "BALANCE_ERROR_INTERRUPTED_EXCEPTION", e.getMessage())
		}
		return result
	}
}

