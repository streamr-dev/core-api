package com.unifina.signalpath.blockchain.templates;

import com.unifina.signalpath.blockchain.SolidityModule;

public class PayByUse extends SolidityModule {
	@Override
	public String getCodeTemplate() {
		String streamrAddress = "0xb3428050ea2448ed2e4409be47e1a50ebac0b2d2";

		// TODO: should unitCost/recipient be public?
		//   + These are supplied to constructor, so they show up on canvas already
		//   - When manipulating from CLI it would be useful (for demo not so important)
		return  "pragma solidity ^0.4.0;\n" +
				"contract PayByUse {\n" +
				"    address recipient;\n" +
				"    uint unitCost;\n" +
				"    uint public unpaidWei;\n" +
				"    \n" +
				"    event OutOfFunds(uint debt);\n" +
				"    event Paid(uint amount);\n" +
				"    \n" +
				"    function PayByUse(address recipientAddress, uint weiPerUnit) payable {\n" +
				"        unitCost = weiPerUnit;\n" +
				"        recipient = recipientAddress;\n" +
				"    }\n" +
				"    \n" +
				"    function update(uint addedUnits) {\n" +
				"        if (msg.sender != " + streamrAddress + ") { throw; }\n" +
				"        unpaidWei += addedUnits * unitCost;\n" +
				"        this.withdraw();\n" +
				"    }\n" +
				"    \n" +
				"    function withdraw() {\n" +
				"        var sendAmount = min(unpaidWei, this.balance - 10);\n" +
				"        if (sendAmount > 0) {\n" +
				"            if (recipient.send(sendAmount)) {\n" +
				"                unpaidWei -= sendAmount;\n" +
				"                Paid(sendAmount);\n" +
				"            }\n" +
				"        }\n" +
				"        if (unpaidWei > 0) {\n" +
				"            OutOfFunds(unpaidWei);\n" +
				"        }\n" +
				"    }\n" +
				"    \n" +
				"    function min(uint x, uint y) constant internal returns (uint) {\n" +
				"        return (x < y) ? x : y;\n" +
				"    }\n" +
				"    \n" +
				"    // to \"top-up\", the payer simply sends money to this contract\n" +
				"    function () payable {}\n" +
				"}";
	}
}
