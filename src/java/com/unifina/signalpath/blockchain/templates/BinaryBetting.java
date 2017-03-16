package com.unifina.signalpath.blockchain.templates;

import com.unifina.signalpath.blockchain.SolidityModule;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

public class BinaryBetting extends SolidityModule {
	@Override
	public String getCodeTemplate() {
		String streamrAddress = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.address");

		// TODO: should unitCost/recipient be public?
		//   + These are supplied to constructor, so they show up on canvas already
		//   - When manipulating from CLI it would be useful (for demo not so important)
		return  "pragma solidity ^0.4.6;\n" +
				"\n" +
				"// Binary bet has two outcomes: zero or one\n" +
				"contract BinaryBetting {\n" +
				"    uint8 public OUTCOME_COUNT = 2;\n" +
				"    uint public MINIMUM_BET = 1 wei;\n" +
				"\n" +
				"    event RoundStarted(uint id, uint closeTime);\n" +
				"    event RoundCancelled(uint id);\n" +
				"    event RoundResolved(uint id, uint8 withOutcome);\n" +
				"    event BetsPlaced(uint id, uint onOutcome0, uint onOutcome1);\n" +
				"\n" +
				"    // Betting rounds are limited-time and winners-take-all\n" +
				"    // Round is either:\n" +
				"    //   - taking bets (isActive, before closeTime)\n" +
				"    //   - closed, not taking bets and waiting for resolveRound (isActive, after closeTime)\n" +
				"    //   - cleared, ether has been distributed (!isActive)\n" +
				"    mapping(uint => Round) rounds;\n" +
				"    mapping(uint => bool) public roundIsActive;\n" +
				"\n" +
				"    struct Round {\n" +
				"        uint closeTime;\n" +
				"        mapping(uint8 => Bet[]) bets;\n" +
				"        mapping(uint8 => uint) valueOnOutcome;\n" +
				"        uint totalValue;\n" +
				"    }\n" +
				"\n" +
				"    struct Bet {\n" +
				"        address account;\n" +
				"        uint value;\n" +
				"        uint8 outcome;\n" +
				"    }\n" +
				"\n" +
				"    function openRound(uint id, uint closeTime) admin {\n" +
				"        if (roundIsActive[id]) { throw; }\n" +
				"        if (closeTime < now) { throw; }\n" +
				"        \n" +
				"        rounds[id] = Round(closeTime, 0);\n" +
				"        roundIsActive[id] = true;\n" +
				"        RoundStarted(id, closeTime);\n" +
				"    }\n" +
				"\n" +
				"    function placeBet(uint id, uint8 outcome) payable {\n" +
				"        if (!roundIsActive[id]) { throw; }\n" +
				"        if (rounds[id].closeTime < now) { throw; }\n" +
				"        if (outcome >= OUTCOME_COUNT) { throw; }\n" +
				"        if (msg.value < MINIMUM_BET) { throw; }\n" +
				"        \n" +
				"        var newBet = Bet(msg.sender, msg.value, outcome);\n" +
				"        rounds[id].bets[outcome].push(newBet);\n" +
				"        rounds[id].valueOnOutcome[outcome] += msg.value;\n" +
				"        rounds[id].totalValue += msg.value;\n" +
				"        \n" +
				"        BetsPlaced(id, rounds[id].valueOnOutcome[0], rounds[id].valueOnOutcome[1]);\n" +
				"    }\n" +
				"\n" +
				"    function resolveRound(uint id, uint8 correctOutcome) admin {\n" +
				"        if (!roundIsActive[id]) { throw; }\n" +
				"        if (correctOutcome >= OUTCOME_COUNT) { throw; }\n" +
				"        \n" +
				"        // if there was no \"other side\", cancel the bet instead of dividing the winnings\n" +
				"        var winnersValue = rounds[id].valueOnOutcome[correctOutcome];\n" +
				"        var totalValue = rounds[id].totalValue;\n" +
				"        if (winnersValue == 0 || winnersValue == totalValue) {\n" +
				"            cancelRound(id);\n" +
				"            return;\n" +
				"        }\n" +
				"        roundIsActive[id] = false;\n" +
				"        \n" +
				"        var winners = rounds[id].bets[correctOutcome];\n" +
				"        for (uint i = 0; i < winners.length; i++) {\n" +
				"            var bet = winners[i];\n" +
				"            var share = bet.value * totalValue / winnersValue;\n" +
				"            payouts[bet.account] += share;\n" +
				"        }\n" +
				"        \n" +
				"        RoundResolved(id, correctOutcome);\n" +
				"    }\n" +
				"\n" +
				"    // return all funds\n" +
				"    function cancelRound(uint id) admin {\n" +
				"        if (!roundIsActive[id]) { throw; }\n" +
				"        roundIsActive[id] = false;\n" +
				"        \n" +
				"        for (uint8 i = 0; i < OUTCOME_COUNT; i++) {\n" +
				"            var bets = rounds[id].bets[i];\n" +
				"            for (uint j = 0; j < bets.length; j++) {\n" +
				"                payouts[bets[j].account] += bets[j].value;\n" +
				"            }\n" +
				"        }\n" +
				"        \n" +
				"        RoundCancelled(id);\n" +
				"    }\n" +
				"\n" +
				"    // Safe sending: each player can withdraw() their funds separately so they can only mess up themselves\n" +
				"    //   see https://github.com/ConsenSys/smart-contract-best-practices#favor-pull-over-push-payments\n" +
				"    mapping(address => uint) payouts;\n" +
				"    function withdraw() {\n" +
				"        if (payouts[msg.sender] == 0) { throw; }\n" +
				"        var payout = payouts[msg.sender];\n" +
				"        payouts[msg.sender] = 0;\n" +
				"        if (!msg.sender.send(payout)) {\n" +
				"            payouts[msg.sender] = payout;\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    modifier admin {\n" +
				"        if (msg.sender != " + streamrAddress + ") { throw; }\n" +
				"        _;\n" +
				"    }\n" +
				"}\n";
	}
}
