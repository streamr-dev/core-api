package com.unifina.signalpath.blockchain.templates;

import com.unifina.signalpath.blockchain.SolidityCompileDeploy;

public class PayByUse extends SolidityCompileDeploy {
	@Override
	public String getCodeTemplate() {
		//   + These are supplied to constructor, so they show up on canvas already
		//   - When manipulating from CLI it would be useful (for demo not so important)
		return  "pragma solidity ^0.4.8;\n" +
				"contract PayByUse {\n" +
				"    address public recipient;\n" +
				"    uint public unitPriceWei;\n" +
				"    uint public unpaidWei;\n" +
				"    address public owner;\n" +
				"    \n" +
				"    event OutOfFunds(uint debt);\n" +
				"    event Paid(uint amount);\n" +
				"    event Recipient(uint balance);\n" +
				"    event Contract(uint balance);\n" +
				"    \n" +
				"    function PayByUse(address recipientAddress, uint unitPrice_wei) payable {\n" +
				"        unitPriceWei = unitPrice_wei;\n" +
				"        recipient = recipientAddress;\n" +
				"        owner = msg.sender;\n" +
				"    }\n" +
				"    \n" +
				"    function payOut() {\n" +
				"        var sendAmount = min(unpaidWei, this.balance);\n" +
				"        if (sendAmount > 0) {\n" +
				"            if (recipient.send(sendAmount)) {\n" +
				"                unpaidWei -= sendAmount;\n" +
				"                Paid(sendAmount);\n" +
				"            }\n" +
				"        }\n" +
				"        if (this.balance == 0) {\n" +
				"            OutOfFunds(unpaidWei);\n" +
				"        }\n" +
				"        Contract(this.balance);\n" +
				"        Recipient(recipient.balance);\n" +
				"    }\n" +
				"    \n" +
				"    function update(uint addedUnits) onlyOwner {\n" +
				"        unpaidWei += addedUnits * unitPriceWei;\n" +
				"        this.payOut();\n" +
				"    }\n" +
				"    \n" +
				"    function kill() onlyOwner {\n" +
				"        selfdestruct(msg.sender);\n" +
				"    }\n" +
				"    \n" +
				"    modifier onlyOwner {\n" +
				"        if (msg.sender != owner) { throw; }\n" +
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
