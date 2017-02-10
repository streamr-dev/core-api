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
				"    uint unitPriceWei;\n" +
				"    uint public unpaidWei;\n" +
				"    \n" +
				"    event OutOfFunds(uint debt);\n" +
				"    event Paid(uint amount);\n" +
				"    \n" +
				"    function PayByUse(address recipientAddress, uint unitPrice_wei) payable {\n" +
				"        unitPriceWei = unitPrice_wei;\n" +
				"        recipient = recipientAddress;\n" +
				"    }\n" +
				"    \n" +
				"    function payOut() {\n" +
				"        var sendAmount = min(unpaidWei, this.balance);\n" +
				"        if (sendAmount > 0) {\n" +
				"            unpaidWei -= sendAmount;\n" +
				"            if (recipient.send(sendAmount)) {\n" +
				"                Paid(sendAmount);\n" +
				"            } else {\n" +
				"                unpaidWei += sendAmount;\n" +
				"            }\n" +
				"        }\n" +
				"        if (this.balance == 0) {\n" +
				"            OutOfFunds(unpaidWei);\n" +
				"        }\n" +
				"    }\n" +
				"    \n" +
				"    function update(uint addedUnits) canvasCommand {\n" +
				"        unpaidWei += addedUnits * unitPriceWei;\n" +
				"        this.payOut();\n" +
				"    }\n" +
				"    \n" +
				"    function kill() canvasCommand {\n" +
				"        selfdestruct(msg.sender);\n" +
				"    }\n" +
				"    \n" +
				"    modifier canvasCommand {\n" +
				"        if (msg.sender != " + streamrAddress + ") { throw; }\n" +
				"        _;\n" +
				"    }\n" +
				"    \n" +
				"    function min(uint x, uint y) constant internal returns (uint) {\n" +
				"        return (x < y) ? x : y;\n" +
				"    }\n" +
				"    \n" +
				"    // to \"top-up\", the payer simply sends ether to this contract's address\n" +
				"    function () payable {}\n" +
				"}\n";
	}
}
