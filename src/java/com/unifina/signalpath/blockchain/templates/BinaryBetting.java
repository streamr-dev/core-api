package com.unifina.signalpath.blockchain.templates;

import com.unifina.signalpath.blockchain.SolidityModule;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;

public class BinaryBetting extends SolidityModule {
	@Override
	public String getCodeTemplate() {
		return  "pragma solidity ^0.4.6;\n" +
				"\n" +
				"// Binary bet has two outcomes: zero or one\n" +
				"contract BinaryBetting {\n" +
				"    uint8 public constant OUTCOME_COUNT = 2;\n" +
				"    uint public constant MINIMUM_BET = 1 wei;\n" +
				"\n" +
				"    event Round(uint id);\n" +
				"    event RoundStarted(uint closeTime);\n" +
				"    event RoundCancelled();\n" +
				"    event RoundResolved(uint8 withOutcome);\n" +
				"    event BetsPlaced(uint onOutcome0, uint onOutcome1);\n" +
				"    event Error(string message);\n" +
				"\n" +
				"    // Betting rounds are limited-time and winners-take-all\n" +
				"    // Round is either:\n" +
				"    //   - taking bets (isActive, before closeTime)\n" +
				"    //   - closed, not taking bets and waiting for resolveRound (isActive, after closeTime)\n" +
				"    //   - cleared, ether has been distributed (!isActive)\n" +
				"    mapping(uint => BettingRound) rounds;\n" +
				"    mapping(uint => bool) public roundIsActive;\n" +
				"\n" +
				"    struct BettingRound {\n" +
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
				"        if (roundIsActive[id]) { Error(\"Round already started\"); return; }\n" +
				"        if (closeTime < now) { Error(\"closeTime must be in future\"); return; }\n" +
				"        \n" +
				"        rounds[id] = BettingRound(closeTime, 0);\n" +
				"        for (uint8 i = 0; i < OUTCOME_COUNT; i++) {\n" +
				"            delete rounds[id].bets[i];\n" +
				"            rounds[id].valueOnOutcome[i] = 0;\n" +
				"        }\n" +
				"        roundIsActive[id] = true;\n" +
				"        Round(id);\n" +
				"        RoundStarted(closeTime);\n" +
				"        BetsPlaced(0, 0);\n" +
				"    }\n" +
				"\n" +
				"    function placeBet(uint id, uint8 outcome) payable {\n" +
				"        if (!roundIsActive[id]) { Error(\"Round not started\"); return; }\n" +
				"        if (rounds[id].closeTime < now) { Error(\"Round already closed\"); return; }\n" +
				"        if (outcome >= OUTCOME_COUNT) { Error(\"Bad outcome value, try 0 or 1\"); return; }\n" +
				"        if (msg.value < MINIMUM_BET) { Error(\"Must send enough ether with transaction\"); return; }\n" +
				"        \n" +
				"        var newBet = Bet(msg.sender, msg.value, outcome);\n" +
				"        rounds[id].bets[outcome].push(newBet);\n" +
				"        rounds[id].valueOnOutcome[outcome] += msg.value;\n" +
				"        rounds[id].totalValue += msg.value;\n" +
				"        \n" +
				"        Round(id);\n" +
				"        BetsPlaced(rounds[id].valueOnOutcome[0], rounds[id].valueOnOutcome[1]);\n" +
				"    }\n" +
				"\n" +
				"    function resolveRound(uint id, uint8 correctOutcome) admin {\n" +
				"        if (!roundIsActive[id]) { Error(\"Round not started\"); return; }\n" +
				"        if (correctOutcome >= OUTCOME_COUNT) { Error(\"Bad outcome value, try 0 or 1\"); return; }\n" +
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
				"        // attempt payout; if it fails, the players can still call withdraw()\n" +
				"        for (i = 0; i < winners.length; i++) {\n" +
				"            bet = winners[i];\n" +
				"            payOut(bet.account);\n" +
				"        }\n" +
				"        \n" +
				"        Round(id);\n" +
				"        RoundResolved(correctOutcome);\n" +
				"        BetsPlaced(rounds[id].valueOnOutcome[0], rounds[id].valueOnOutcome[1]);\n" +
				"    }\n" +
				"\n" +
				"    // return all funds\n" +
				"    function cancelRound(uint id) admin {\n" +
				"        if (!roundIsActive[id]) { Error(\"Round not started\"); return; }\n" +
				"        roundIsActive[id] = false;\n" +
				"        \n" +
				"        for (uint8 i = 0; i < OUTCOME_COUNT; i++) {\n" +
				"            var bets = rounds[id].bets[i];\n" +
				"            for (uint j = 0; j < bets.length; j++) {\n" +
				"                payouts[bets[j].account] += bets[j].value;\n" +
				"            }\n" +
				"        }\n" +
				"        \n" +
				"        // attempt payout; if it fails, the players can still call withdraw()\n" +
				"        for (i = 0; i < OUTCOME_COUNT; i++) {\n" +
				"            bets = rounds[id].bets[i];\n" +
				"            for (j = 0; j < bets.length; j++) {\n" +
				"                payOut(bets[j].account);\n" +
				"            }\n" +
				"        }\n" +
				"        \n" +
				"        Round(id);\n" +
				"        RoundCancelled();\n" +
				"        BetsPlaced(rounds[id].valueOnOutcome[0], rounds[id].valueOnOutcome[1]);\n" +
				"    }\n" +
				"\n" +
				"    // Backup in case send fails during resolveRound or cancelRound\n" +
				"    function withdraw() {\n" +
				"        payOut(msg.sender);\n" +
				"    }\n" +
				"\n" +
				"    mapping(address => uint) payouts;\n" +
				"    function payOut(address target) private {\n" +
				"        if (payouts[target] == 0) { return; }\n" +
				"        var payout = payouts[target];\n" +
				"        payouts[target] = 0;\n" +
				"        if (!target.send(payout)) {\n" +
				"            payouts[target] = payout;\n" +
				"        }\n" +
				"    }\n" +
				"    \n" +
				"    function kill() admin {\n" +
				"        selfdestruct(" + ADDRESS_PLACEHOLDER + ");\n" +
				"    }\n" +
				"\n" +
				"    modifier admin {\n" +
				"        if (msg.sender == " + ADDRESS_PLACEHOLDER + ") {\n" +
				"            _;\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
	}
}
