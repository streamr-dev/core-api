package com.unifina.service

import com.unifina.domain.User
import com.unifina.utils.ApplicationConfig
import com.unifina.utils.ApplicationConfigException
import com.unifina.utils.EthereumConfig
import org.web3j.exceptions.MessageDecodingException
import org.web3j.protocol.Web3j

import java.util.concurrent.ExecutionException

class BalanceService {
	Web3jHelperService web3jHelperService

	Map<String, BigInteger> getDatacoinBalances(User user) throws InterruptedException, ExecutionException, MessageDecodingException {
		Web3j web3j
		Map<String, BigInteger> result = new LinkedHashMap<String, BigInteger>()
		try {
			EthereumConfig ethereumConf = new EthereumConfig(ApplicationConfig.getString("streamr.ethereum.defaultNetwork"))
			web3j = ethereumConf.getWeb3j(EthereumConfig.RpcConnectionMethod.HTTP)
			String dataCoinAddress = ApplicationConfig.getString("streamr.ethereum.datacoinAddress");
			if (dataCoinAddress == null) {
				throw new ApplicationConfigException("No datacoin address found in config");
			}
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
		} finally {
			if (web3j != null) {
				web3j.shutdown()
			}
		}
		return result
	}
}

