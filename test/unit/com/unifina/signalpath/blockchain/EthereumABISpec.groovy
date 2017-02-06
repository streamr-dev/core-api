package com.unifina.signalpath.blockchain

import spock.lang.Specification

class EthereumABISpec extends Specification {

	EthereumABI abi

	def setup() {
		abi = new EthereumABI("""
			 [{
			"constant": true,
			"inputs": [],
			"name": "weiPerUnit",
			"outputs": [{
				"name": "",
				"type": "uint256"
			}],
			"payable": false,
			"type": "function"
		}, {
			"constant": false,
			"inputs": [],
			"name": "withdraw",
			"outputs": [],
			"payable": false,
			"type": "function"
		}, {
			"constant": true,
			"inputs": [],
			"name": "STREAMR_ADDRESS",
			"outputs": [{
				"name": "",
				"type": "address"
			}],
			"payable": false,
			"type": "function"
		}, {
			"constant": true,
			"inputs": [],
			"name": "recipient",
			"outputs": [{
				"name": "",
				"type": "address"
			}],
			"payable": false,
			"type": "function"
		}, {
			"constant": true,
			"inputs": [],
			"name": "unpaidWei",
			"outputs": [{
				"name": "",
				"type": "uint256"
			}],
			"payable": false,
			"type": "function"
		}, {
			"constant": false,
			"inputs": [{
				"name": "addedUnits",
				"type": "uint256"
			}],
			"name": "update",
			"outputs": [],
			"payable": false,
			"type": "function"
		}, {
			"inputs": [{
				"name": "_recipient",
				"type": "address"
			}, {
				"name": "_weiPerUnit",
				"type": "uint256"
			}],
			"payable": true,
			"type": "constructor"
		}, {
			"payable": true,
			"type": "fallback"
		}, {
			"anonymous": false,
			"inputs": [{
				"indexed": false,
				"name": "debt",
				"type": "uint256"
			}],
			"name": "OutOfFunds",
			"type": "event"
		}]
		""")
	}

	def cleanup() {

	}

	void "EthereumABI parses the constructor"() {
		EthereumABI.Function c = abi.constructor

		expect:
		c.inputs.size() == 2
		c.inputs[0].name == "_recipient"
		c.inputs[0].type == "address"
		c.inputs[1].name == "_weiPerUnit"
		c.inputs[1].type == "uint256"
	}

	void "EthereumABI parses functions"() {
		List<EthereumABI.Function> functions = abi.functions

		expect:
		functions.size() == 7
	}

	void "EthereumABI parses events"() {
		List<EthereumABI.Event> events = abi.events

		expect:
		events.size() == 1
		events[0].inputs.size() == 1
		events[0].inputs[0].name == "debt"
		events[0].inputs[0].type == "uint256"
	}
}
