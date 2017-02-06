package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.*;

/**
 * Maps Ethereum types to Streamr types
 * NB: obviously casting e.g. uint256 to Double loses precision (range should be sufficient), so
 * 		this can be a VERY bad idea for e.g. financial transactions! Streamr needs BigInteger support for that.
 * 	Not handled: fixed-size arrays (int[3]), dynamic-size arrays (uint[]), they will be "Object"
 */
public class EthereumToStreamrTypes {

	private static boolean isStringType(String type) {
		return type.equals("address") // 20 bytes
				|| type.equals("string") // variable length
				|| type.startsWith("function") // 20-byte address + 4-byte selector
				|| type.startsWith("bytes"); // variable length
	}

	public static Input asInput(String type, String name, AbstractSignalPathModule owner) {
		if (isStringType(type)) {
			return new StringInput(owner, name);
		} else if (type.equals("bool")) {
			return new BooleanInput(owner, name);
		} else if (type.startsWith("fixed")
				|| type.startsWith("ufixed")
				|| type.startsWith("uint")
				|| type.startsWith("int")) {
			return new TimeSeriesInput(owner, name);
		} else {
			return new Input(owner, name, "Object");
		}
	}

	public static Parameter asParameter(String type, String name, AbstractSignalPathModule owner) {
		if (isStringType(type)) {
			return new StringParameter(owner, name, "");
		} else if (type.equals("bool")) {
			return new BooleanParameter(owner, name, true);
		} else if (type.startsWith("fixed")
				|| type.startsWith("ufixed")) {
			return new DoubleParameter(owner, name, 0D);
		} else if (type.startsWith("uint")
				|| type.startsWith("int")) {
			return new IntegerParameter(owner, name, 0);
		} else {
			throw new IllegalArgumentException("Sorry, I don't know how to handle type "+type+" as a parameter!");
		}
	}

	public static Output asOutput(String type, String name, AbstractSignalPathModule owner) {
		if (isStringType(type)) {
			return new StringOutput(owner, name);
		} else if (type.equals("bool")) {
			return new BooleanOutput(owner, name);
		} else if (type.startsWith("fixed")
				|| type.startsWith("ufixed")
				|| type.startsWith("uint")
				|| type.startsWith("int")) {
			return new TimeSeriesOutput(owner, name);
		} else {
			return new Output(owner, name, "Object");
		}
	}
}
