package com.unifina.signalpath.blockchain

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import grails.test.mixin.Mock
import groovy.json.JsonSlurper
import spock.lang.Specification

@Mock([IntegrationKey, SecUser])
class SolidityModuleSpec extends Specification {
	SolidityModule module

	def setup() {
		module = new SolidityModule()
		module.init()

		module.globals = new Globals()

		module.web3 = Stub(SolidityModule.StreamrWeb3Interface) {
			compile(_) >> { EthereumContract.fromMap(applyConfig.contract) }
			deploy(_) >> { EthereumContract.fromMap(applyConfig.contract) }
		}

		// mock the key for ethereum account
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false)
		IntegrationKey key = new IntegrationKey(service: IntegrationKey.Service.ETHEREUM, name: "test key", json: '{"privateKey":"lol","address":"0x1234"}', user: user)
		key.id = "sgKjr1eHQpqTmwz3vK3DqwUK1wFlrfRJa9mnf_xTeFJQ"
		key.save(failOnError: true, validate: true)

		// set ethereum account
		module.inputs[0].setConfiguration(applyConfig.params[0])
	}

	void "Module parameters correspond to non-payable constructor arguments"() {
		when:
		applyConfig.contract.abi[2].payable = false
		module.onConfiguration(applyConfig)

		then:
		module.inputs*.name == ["ethAccount", "value", "addr"]
	}

	void "Module parameters correspond to payable constructor arguments"() {
		when:
		applyConfig.contract.abi[2].payable = true
		module.onConfiguration(applyConfig)

		then:
		module.inputs*.name == ["ethAccount", "value", "addr", "initial ETH"]
	}

	Map applyConfig = new JsonSlurper().parseText('''
{
    "contract":
    {
        "address": "0x60f78aa68266c87fecec6dcb27672455111bb347",
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
    "code": "pragma solidity ^0.4.8;\\n\\ncontract RuuviDemo {\\n    \\n    int public lol;\\n    address public lodli;\\n    \\n    function RuuviDemo(int value, address addr) {\\n        lol = value;\\n        lodli = addr;\\n    }\\n}",
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
        "value": "0x6e6adf6e579d83f8f1bc388a392c1a130b8f8d0cae6250612eb2aab4e945b1f0",
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
        "uiResendAll":
        {
            "value": false,
            "type": "boolean"
        },
        "network":
        {
            "possibleValues": [
            {
                "text": "ropsten",
                "value": "ropsten"
            },
            {
                "text": "rinkeby",
                "value": "rinkeby"
            }],
            "value": "rinkeby",
            "type": "string"
        },
        "gasPriceGWei":
        {
            "value": 20,
            "type": "double"
        }
    }
}
	''')
}
