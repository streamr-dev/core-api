package com.unifina.signalpath.blockchain

import com.google.gson.Gson
import com.unifina.ModuleTestingSpecification
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.serialization.SerializerImpl
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.service.SerializationService
import com.unifina.utils.Globals
import grails.converters.JSON
import grails.test.mixin.Mock
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt

import java.lang.reflect.InvocationTargetException

@Mock([IntegrationKey, SecUser])
class SolidityCompileDeploySpec extends ModuleTestingSpecification {
	public static final String TXHASH = "0x123";
	public static final String DEPLOY_ADDRESS = "0x60f78aa68266c87fecec6dcb27672455111bb347";
	SolidityCompileDeploy module



	public static class ModifiedSolidityCompileDeploy extends SolidityCompileDeploy{
		def deployArgs
		def sentWei
		def mockWeb3j

		public ModifiedSolidityCompileDeploy(Web3j web3j){
			mockWeb3j = web3j
		}

		@Override
		protected Web3j getWeb3j() {
			return mockWeb3j;
		}
		@Override
		protected String deploy(String bytecode, List<Object> args, BigInteger sendWei) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			deployArgs = args;
			sentWei = sendWei;
			super.deploy(bytecode,args,sendWei);
		}

	}
	def setup() {
		// mock the key for ethereum account
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false)
		IntegrationKey key = new IntegrationKey(service: IntegrationKey.Service.ETHEREUM, name: "test key", json: '{"privateKey":"0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0","address":"0x1234"}', user: user, idInService: "0x1234")
		key.id = "sgKjr1eHQpqTmwz3vK3DqwUK1wFlrfRJa9mnf_xTeFJQ"
		key.save(failOnError: true, validate: true)

		mockBean(EthereumIntegrationKeyService.class, Stub(EthereumIntegrationKeyService) {
			getAllPrivateKeysForUser(user) >> [key];
			decryptPrivateKey(_) >> {k ->
				Map json = JSON.parse(k[0].json)
				return (String) json.privateKey;
			}
		})

		module = new ModifiedSolidityCompileDeploy(mockWeb3j);

		module.globals = mockGlobals([:], user)
		module.init()

		// set ethereum account
		module.inputs[0].setConfiguration(applyConfig.params[0])
	}

	def mockWeb3j = Stub(Web3j) {
		ethGetTransactionCount(_, _) >> { String address, DefaultBlockParameterName latest ->
			return new Request(){
				public EthGetTransactionCount send() throws IOException {
					return new EthGetTransactionCount(){
						@Override
						public BigInteger getTransactionCount(){
							return 1;
						}
					};
				}
			};
		}
		ethSendRawTransaction(_) >> { String bytesHex ->
			return new Request(){
				public EthSendTransaction send() throws IOException {
					return new EthSendTransaction(){
						@Override
						public String getTransactionHash(){
							return TXHASH;
						}
					};
				}
			};
		}
		ethGetTransactionReceipt(_) >> { String txHash ->
			return new Request(){
				public EthGetTransactionReceipt send() throws IOException {
					return new EthGetTransactionReceipt(){
						@Override
						public TransactionReceipt getResult(){
							return new TransactionReceipt() {
								@Override
								public String getContractAddress() {
									return DEPLOY_ADDRESS;
								}
							}
						}
					};
				}

			}

		}
	}

	void "Module parameters correspond to non-payable constructor arguments"() {
		when:
		applyConfig.contract.abi[2].payable = false
		applyConfig.code = unpayable_constructor;

		module.onConfiguration(applyConfig)

		then:
		module.inputs*.name == ["ethAccount", "value", "addr"]
	}

	void "Module parameters correspond to payable constructor arguments"() {
		when:
		applyConfig.contract.abi[2].payable = true
		applyConfig.code = payable_constructor;
		module.onConfiguration(applyConfig)

		then:
		module.inputs*.name == ["ethAccount", "value", "addr", "initial ETH"]
	}

	void "After configuration the contract can be pulled"() {
		when:
		applyConfig.contract.abi[2].payable = true
		applyConfig.code = payable_constructor;
		module.onConfiguration(applyConfig)

		then:
		module.pullValue(module.getOutput("contract")) != null
	}

	void "Values are sent correctly to deploy function with non-payable constructor"() {
		when:
		applyConfig.contract.abi[2].payable = false
		applyConfig.code = unpayable_constructor;

		applyConfig << [deploy: true]
		module.onConfiguration(applyConfig)

		then:
		module.deployArgs == [3, "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1"]
		module.sentWei == 0
	}

	void "Values are sent correctly to deploy function with payable constructor"() {
		when:
		applyConfig.contract.abi[2].payable = true
		applyConfig.code = payable_constructor
		applyConfig.params << initialEthInputConfig
		applyConfig << [deploy: true]
		module.onConfiguration(applyConfig)

		then:
		module.deployArgs == [3, "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1"]
		module.sentWei == 100000000000000000
	}

	void "attacker can't use another user's key to deploy"() {
		def attacker = new SecUser(
			name: "attacker",
			username: "attacker",
			password: "pass"
		).save(failOnError: true, validate: false)

		module.setGlobals(new Globals(module.globals.signalPathContext, attacker))

		when:
		applyConfig.contract.abi[2].payable = true
		applyConfig.code = payable_constructor
		applyConfig.params << initialEthInputConfig
		applyConfig << [deploy: true]
		module.onConfiguration(applyConfig)

		then:
		def e = thrown(RuntimeException)
		e.message.contains("Access denied to Ethereum private key")
	}

	void "Contract can be pulled after serialisation/deserialisation"() {
		def serializationService = new SerializationService()
		serializationService.serializer = new SerializerImpl(this.getClass().getClassLoader())

		when:
		applyConfig.contract.abi[2].payable = true
		applyConfig.code = payable_constructor
		applyConfig.remove("deploy")
		module = new ModifiedSolidityCompileDeploy(null)
		module.onConfiguration(applyConfig)
		byte[] bytes = serializationService.serialize(module)
		module = serializationService.deserialize(bytes)

		then:
		module.pullValue(module.getOutput("contract")) != null
	}

	void "compile works with null ethAccount"() {
		when:
		applyConfig.params[0].value = null
		// set ethereum account
		module.inputs[0].setConfiguration(applyConfig.params[0])
		module.onConfiguration(applyConfig)

		then:
		module.pullValue(module.getOutput("contract")) != null
	}

	void "deploy doesn't work with null ethAccount"() {
		when:
		applyConfig.params[0].value = null
		applyConfig << [deploy: true]
		// set ethereum account
		module.inputs[0].setConfiguration(applyConfig.params[0])
		module.onConfiguration(applyConfig)

		then:
		def e = thrown(RuntimeException)
		e.message.contains("no private key is selected")
	}

	static Map applyConfig = new Gson().fromJson('''
{
    "contract":
    {
        "abi": [
        {
            "constant": true,
            "payable": false,
            "inputs": [],
            "name": "lol",
            "outputs": [
            {
                "name": "",
                "type": "int256"
            }],
            "type": "function"
        },
        {
            "constant": true,
            "payable": false,
            "inputs": [],
            "name": "lodli",
            "outputs": [
            {
                "name": "",
                "type": "address"
            }],
            "type": "function"
        },
        {
            "payable": false,
            "inputs": [
            {
                "name": "value",
                "type": "int256"
            },
            {
                "name": "addr",
                "type": "address"
            }],
            "type": "constructor"
        }]
    },
    "hash": 1,
    "code": "pragma solidity ^0.4.8;\\n\\ncontract RuuviDemo {\\n    \\n    int public lol;\\n    address public lodli;\\n    \\n    function RuuviDemo(int value, address addr) /*payable*/ {\\n        lol = value;\\n        lodli = addr;\\n    }\\n}",
    "params": [
    {
        "canConnect": false,
        "updateOnChange": true,
        "export": false,
        "possibleValues": [
        {
            "name": "(none)",
            "value": null
        },
        {
            "name": "acco 1",
            "value": "sgKjr1eHQpqTmwz3vK3DqwUK1wFlrfRJa9mnf_xTeFJQ"
        }],
        "connected": false,
        "type": "String",
        "requiresConnection": false,
        "canToggleDrivingInput": true,
        "id": "ep_5RFpE2iKTPawMMKNx0ty8Q",
        "name": "ethAccount",
        "drivingInput": false,
        "value": "sgKjr1eHQpqTmwz3vK3DqwUK1wFlrfRJa9mnf_xTeFJQ",
        "longName": "SolidityModule.ethAccount",
        "defaultValue": null,
        "acceptedTypes": ["String"]
    },
    {
        "canConnect": false,
        "export": false,
        "connected": false,
        "type": "Double",
        "requiresConnection": false,
        "canToggleDrivingInput": false,
        "id": "ep_iIo0Y5dVQYWOnEj_HOdjXQ",
        "name": "value",
        "drivingInput": false,
        "longName": "SolidityModule.value",
        "value": 3,
        "defaultValue": 0,
        "acceptedTypes": ["Double"]
    },
    {
        "canConnect": false,
        "export": false,
        "connected": false,
        "isTextArea": false,
        "type": "String",
        "requiresConnection": false,
        "canToggleDrivingInput": false,
        "id": "ep_W5RsHk2DRVCkpx-LJVeR5w",
        "name": "addr",
        "drivingInput": false,
        "longName": "SolidityModule.addr",
        "value": "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1",
        "defaultValue": "",
        "acceptedTypes": ["String"]
    }],
    "uiChannel":
    {
        "id": "Sdzrb-HbRyewp-mMO_bT6A",
        "webcomponent": null,
        "name": "SolidityModule"
    },
    "type": "module",
    "id": 1021,
    "canRefresh": false,
    "inputs": [],
    "canClearState": true,
    "compile": true,
    "name": "SolidityModule",
    "layout":
    {
        "position":
        {
            "left": "642px",
            "top": "86px"
        }
    },
    "outputs": [
    {
        "id": "ep_DzKaYIMCTgups-ERdwAYfA",
        "canConnect": true,
        "export": false,
        "name": "contract",
        "connected": true,
        "longName": "SolidityModule.contract",
        "value":
        {
            "address": "0x60f78aa68266c87fecec6dcb27672455111bb347",
            "class": "com.unifina.signalpath.blockchain.EthereumContract",
            "deployed": true,
            "ABI":
            {
                "functions": [
                {
                    "class": "com.unifina.signalpath.blockchain.EthereumABI $ Function "
                },
                {
                    "class": "com.unifina.signalpath.blockchain.EthereumABI $ Function "
                }],
                "events": [],
                "class": "com.unifina.signalpath.blockchain.EthereumABI",
                "constructor":
                {
                    "class": "com.unifina.signalpath.blockchain.EthereumABI $ Function "
                }
            }
        },
        "type": "EthereumContract",
        "targets": ["ep_ofzj_qlUTbu86guv1Tugtw"]
    }],
    "jsModule": "SolidityModule",
    "options":
    {
        "uiResendLast":
        {
            "value": 0,
            "type": "int"
        },
        "network":
        {
            "possibleValues": [
            {
                "text": "local",
                "value": "local"
            }
            ],
            "value": "local",
            "type": "string"
        },
        "gasPriceGWei":
        {
            "value": 20,
            "type": "double"
        }
    }
}
	''', Map.class)

	final static String unpayable_constructor=applyConfig.code
	final static String payable_constructor=applyConfig.code.replaceFirst(/\/\*payable\*\//, " payable ")


	static Map initialEthInputConfig = new Gson().fromJson('''{
		"canConnect": true,
		"export": false,
		"connected": false,
		"type": "Double",
		"requiresConnection": false,
		"canToggleDrivingInput": true,
		"id": "ep_VcrH83D7Rci0vSgs_oJA-Q",
		"name": "initial ETH",
		"drivingInput": false,
		"longName": "PayByUse.initial ETH",
		"value": 0.1,
		"defaultValue": 0,
		"acceptedTypes": ["Double"]
	}''', Map.class)

}
