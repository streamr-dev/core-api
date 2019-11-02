package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithSideEffects;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.TimeSeriesOutput;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class GetEthBalance extends ModuleWithSideEffects {
	private StringInput ethAddress = new StringInput(this, "address");
	private TimeSeriesOutput balanceEther = new TimeSeriesOutput(this, "balanceEther");
	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	private transient Web3j web3j;
	protected Web3j getWeb3j() {
		if (web3j == null && ethereumOptions != null) {
			web3j = ethereumOptions.getEthereumOptions().getWeb3j();
		}
		return web3j;
	}

	@Override
	public void init() {
		addInput(ethAddress);
		ethAddress.setDrivingInput(true);
		ethAddress.setCanToggleDrivingInput(false);
		addOutput(balanceEther);

	}
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeTo(options);
		return config;
	}
	@Override
	protected void activateWithSideEffects(){
		try {
			BigInteger balanceWei = getWeb3j().ethGetBalance(ethAddress.getValue(), DefaultBlockParameterName.LATEST).send().getBalance();
			balanceEther.send(new BigDecimal(balanceWei).divide(BigDecimal.TEN.pow(18)).doubleValue());
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}

	}
	@Override
	public void clearState(){


	}
}
