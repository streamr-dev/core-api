package com.unifina.service

import com.unifina.signalpath.blockchain.Web3jHelper
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall

class EthereumService {
	String fetchJoinPartStreamID(String communityAddress) {
		Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		Function getJoinPartStream = new Function("getJoinPartStream", Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList(TypeReference.create(Utf8String.class)))
		Transaction tx = Transaction.createEthCallTransaction(communityAddress, communityAddress, FunctionEncoder.encode(getJoinPartStream))
		EthCall response = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).send()
		Response.Error err = response.getError()
		if (err != null) {
			throw new RuntimeException(err.getMessage())
		}
		List<Type> result = FunctionReturnDecoder.decode(response.getValue(), getJoinPartStream.getOutputParameters())
		return ((Utf8String) result.iterator().next()).getValue()
	}

	/**
	 * Calls operator() getter from contract with public "operator" address:
	 * address public operator;
	 */
	String fetchCommunityAdminsEthereumAddress(String communityAddress) {
		Web3j web3j = Web3jHelper.getWeb3jConnectionFromConfig()
		Function getOwner = new Function("owner", Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList(TypeReference.create(Address.class)))
		Transaction tx = Transaction.createEthCallTransaction(communityAddress, communityAddress, FunctionEncoder.encode(getOwner))
		EthCall response = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).send()
		Response.Error err = response.getError()
		if (err != null) {
			throw new RuntimeException(err.getMessage())
		}
		List<Type> result = FunctionReturnDecoder.decode(response.getValue(), getOwner.getOutputParameters())
		return ((Address) result.iterator().next()).getValue()
	}
}
