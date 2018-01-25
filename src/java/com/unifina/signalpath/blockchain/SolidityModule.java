package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SolidityModule extends ModuleWithUI implements Pullable<EthereumContract> {

	private static final Logger log = Logger.getLogger(SolidityModule.class);

	private final EthereumAccountParameter ethereumAccount = new EthereumAccountParameter(this, "ethAccount");
	private Output<EthereumContract> contractOutput = null;

	private String code = null;
	private EthereumContract contract = null;
	private DoubleParameter sendEtherParam = new DoubleParameter(this, "initial ETH", 0.0);

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	@Override
	public void init() {
		addInput(ethereumAccount);
		ethereumAccount.setUpdateOnChange(true);
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

	/** Override to provide contract template that will be compiled when module is added to canvas */
	public String getCodeTemplate() {
		return null;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeTo(options);

		config.put("code", code);
		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (config.containsKey("code")) {
			code = config.get("code").toString();
		} else {
			code = getCodeTemplate();
		}

		if (config.containsKey("contract")) {
			contract = EthereumContract.fromMap(MapTraversal.getMap(config, "contract"));
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
		final StreamrWeb3Interface web3 = createWeb3Interface();

		try {
			if (config.containsKey("compile") || (code != null && !code.trim().isEmpty() && ethereumAccount.getAddress() != null && (contract == null || !contract.isDeployed()))) {
				contract = web3.compile(code);
			}
			if (config.containsKey("deploy")) {
				// Make sure the contract is compiled
				if (contract == null) {
					contract = web3.compile(code);
				}

				if (!contract.isDeployed()) {
					EthereumABI.Function constructor = contract.getABI().getConstructor();
					String sendWei = "0";
					Stack<Object> args = new Stack<>();

					if (constructor != null) {
						List<Map> params = (List) config.get("params");
						// skip first parameter (ethAccount, not constructor parameter)
						for (Map param : params.subList(1, params.size())) {
							args.push(param.get("value"));
						}
						// for payable constructors, sendEtherParam is added in params after the ordinary function arguments
						// value can't be read from sendEtherParam.getValue because it's not added to the module until createParameters is called (so it exists in config but not in module object)
						if (constructor.payable) {
							BigDecimal sendEtherParamValue = new BigDecimal(args.pop().toString());
							BigDecimal valueWei = sendEtherParamValue.multiply(BigDecimal.TEN.pow(18));
							sendWei = valueWei.toBigInteger().toString();
						}
					}

					contract = web3.deploy(code, args, sendWei, ethereumAccount.getAddress(), ethereumAccount.getPrivateKey());
				}
			}
		} catch (Exception e) {
			if (ExceptionUtils.getRootCause(e) instanceof java.net.ConnectException) {
				log.error("Could not connect to web3 backend!", e);
				throw new RuntimeException("Sorry, we couldn't contact Ethereum at this time. We'll try to fix this soon.");
			} else {
				throw new RuntimeException(e);
			}
		}

		if (contract != null) {
			createContractOutput();
			createParameters(contract.getABI());
		}
	}
	
	protected StreamrWeb3Interface createWeb3Interface() {
		return new StreamrWeb3Interface(ethereumOptions.getServer(), ethereumOptions.getGasPriceWei());
	}

	private void createContractOutput() {
		contractOutput = new EthereumContractOutput(this, "contract");
		addOutput(contractOutput);
	}

	private void createParameters(EthereumABI abi) {
		EthereumABI.Function constructor = abi.getConstructor();
		if (constructor != null) {
			for (EthereumABI.Slot input : constructor.inputs) {
				String name = input.name.replace("_", " ");
				Parameter p = EthereumToStreamrTypes.asParameter(input.type, name, this);
				p.setCanConnect(false);
				p.setCanToggleDrivingInput(false);
				addInput(p);
			}
			if (constructor.payable) {
				addInput(sendEtherParam);
			}
		}
	}

	@Override
	public EthereumContract pullValue(Output output) {
		return contract;
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();
		if (contract != null) {
			contractOutput.send(contract);
		}
	}

}
