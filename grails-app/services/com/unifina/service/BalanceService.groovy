package com.unifina.service

import com.unifina.api.ApiException
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

	@PostConstruct
	void init() {
		final HttpService httpService = new HttpService("https://rinkeby.infura.io") // TODO: Fix address
		final Web3j web3j = Web3j.build(httpService)
		this.web3 = new Web3BalanceImpl(web3j)
	}

	@CompileStatic
	BigInteger checkBalance(String address) throws ApiException {
		try {
			return this.web3.checkBalance(address)
		} catch (ExecutionException e) {
			throw new ApiException(500, "BALANCE_ERROR", e.getCause().getMessage())
		} catch (MessageDecodingException e) {
			throw new ApiException(500, "BALANCE_ERROR", e.getCause().getMessage())
		} catch (InterruptedException e) {
			throw new ApiException(500, "BALANCE_DECODING_ERROR", e.getMessage())
		}
	}
}

interface Web3Balance {
	BigInteger checkBalance(String address) throws InterruptedException, ExecutionException, MessageDecodingException
}

class Web3BalanceImpl implements Web3Balance {
	private Web3j web3

	Web3BalanceImpl(Web3j web3) {
		this.web3 = web3
	}

	@Override
	BigInteger checkBalance(String address) throws InterruptedException, ExecutionException, MessageDecodingException {
		EthGetBalance ethGetBalance = this.web3
			.ethGetBalance(address, DefaultBlockParameterName.LATEST)
			.sendAsync()
			.get()
		final BigInteger balance = ethGetBalance.getBalance()
		return balance
	}
}
