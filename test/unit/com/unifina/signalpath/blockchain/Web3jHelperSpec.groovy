package com.unifina.signalpath.blockchain

import com.unifina.ModuleTestingSpecification
import com.unifina.security.StreamrApi
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.BytesType
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Int
import org.web3j.abi.datatypes.StaticArray
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Uint
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint32

class Web3jHelperSpec extends ModuleTestingSpecification {


	@StreamrApi
	void "instantiateType produces correct Solidity type"() {
		when:
		Object[] nums = [1, 2, "0x123"];
		byte[] bytes = [1, 2, 3];
		byte[][] bytesArray = [bytes, bytes, bytes]
		byte[][][] twoDimBytesArray = [bytesArray, bytesArray, bytesArray]
		Object[][] twoDimIntarray = [nums, nums, nums]

		Type twoDarray = Web3jHelper.instantiateType("uint[][3]", twoDimIntarray)

		then:
		Web3jHelper.instantiateType("bool", false) instanceof Bool
		Web3jHelper.instantiateType("bool", 1) instanceof Bool
		Web3jHelper.instantiateType("int", -123) instanceof Int
		Web3jHelper.instantiateType("int256", -123) instanceof Int256
		Web3jHelper.instantiateType("uint", 123) instanceof Uint
		Web3jHelper.instantiateType("uint256", 123) instanceof Uint256
		Web3jHelper.instantiateType("address", "0x123") instanceof Address
		Web3jHelper.instantiateType("address", BigInteger.ONE) instanceof Address
		Web3jHelper.instantiateType("uint256[]", Arrays.asList(nums)) instanceof DynamicArray
		Web3jHelper.instantiateType("uint256[3]", Arrays.asList(nums)) instanceof StaticArray
		Web3jHelper.instantiateType("uint256[]", nums) instanceof DynamicArray
		Web3jHelper.instantiateType("uint256[3]", nums) instanceof StaticArray
		Web3jHelper.instantiateType("bytes" + bytes.length, bytes) instanceof BytesType
		Web3jHelper.instantiateType("bytes[]", Arrays.asList(bytesArray)) instanceof DynamicArray
		Web3jHelper.instantiateType("bytes[3]", Arrays.asList(bytesArray)) instanceof StaticArray
		Web3jHelper.instantiateType("uint[][]", twoDimIntarray) instanceof DynamicArray
		Web3jHelper.instantiateType("uint[3][]", twoDimIntarray) instanceof DynamicArray
		Web3jHelper.web3jArrayGet((StaticArray) twoDarray, 0,1).getValue().equals(BigInteger.valueOf(2))
		Web3jHelper.instantiateType("bytes[][3]", twoDimBytesArray) instanceof StaticArray


	}
}
