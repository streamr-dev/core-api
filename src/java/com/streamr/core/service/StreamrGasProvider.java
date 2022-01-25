package com.streamr.core.service;

import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

public class StreamrGasProvider extends StaticGasProvider  {
	public static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
	public static final BigInteger GAS_LIMIT = BigInteger.valueOf(6_000_000L);

	public static StreamrGasProvider createStreamrGasProvider() {
		return new StreamrGasProvider(GAS_PRICE, GAS_LIMIT);
	}

	protected StreamrGasProvider(BigInteger gasPrice, BigInteger gasLimit) {
		super(gasPrice, gasLimit);
	}
}
