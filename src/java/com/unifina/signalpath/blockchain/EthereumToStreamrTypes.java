package com.unifina.signalpath.blockchain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.unifina.signalpath.*;
import org.web3j.abi.datatypes.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Ethereum types to Streamr types
 * NB: obviously casting e.g. uint256 to Double loses precision (range should be sufficient), so
 * 		this can be a VERY bad idea for e.g. financial transactions! Streamr needs BigInteger support for that.
 */
public class EthereumToStreamrTypes {

	private static boolean isArrayType(String type) {
		return type.endsWith("]");
	}

	private static boolean isStringType(String type) {
		return type.equals("address") // 20 bytes
				|| type.equals("string") // variable length
				|| type.startsWith("function") // 20-byte address + 4-byte selector
				|| type.startsWith("bytes"); // variable length
	}

	private static boolean isNumberType(String type) {
		return type.startsWith("fixed")
				|| type.startsWith("ufixed")
				|| type.startsWith("uint")
				|| type.startsWith("int");
	}

	public static Input asInput(String type, String name, AbstractSignalPathModule owner) {
		if (isArrayType(type)) {
			return new ListInput(owner, name);
		} else if (isStringType(type)) {
			return new StringInput(owner, name);
		} else if (type.equals("bool")) {
			return new BooleanInput(owner, name);
		} else if (isNumberType(type)) {
			return new TimeSeriesInput(owner, name);
		} else {
			return new Input(owner, name, "Object");
		}
	}

	public static Parameter asParameter(String type, String name, AbstractSignalPathModule owner) {
		if (isArrayType(type)) {
			return new ListParameter(owner, name);
		} else if (isStringType(type)) {
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
		if (isArrayType(type)) {
			return new ListOutput(owner, name);
		} else if (isStringType(type)) {
			return new StringOutput(owner, name);
		} else if (type.equals("bool")) {
			return new BooleanOutput(owner, name);
		} else if (isNumberType(type)) {
			return new TimeSeriesOutput(owner, name);
		} else {
			return new Output(owner, name, "Object");
		}
	}

	public static void convertAndSend(Output output, JsonElement value) {
		if (output instanceof ListOutput) {
			ArrayList val = new Gson().fromJson(value, ArrayList.class);
			output.send(val);
		} else if (output instanceof StringOutput) {
			output.send(value.getAsString());
		} else if (output instanceof BooleanOutput) {
			output.send(value.getAsBoolean());
		} else if (output instanceof TimeSeriesOutput) {
			output.send(value.getAsNumber().doubleValue());
		} else {
			output.send(value.getAsString());
		}
	}

	public static void convertAndSend(Output output, Object value) {
		if (output instanceof ListOutput) {
			List val = (List) value;
			if (isWeb3TypeList(val)) {
				val = convertList(val);
			}
			output.send(val);
		} else if (output instanceof StringOutput) {
			output.send(value);
		} else if (output instanceof BooleanOutput) {
			output.send(Boolean.parseBoolean(value.toString()));
		} else if (output instanceof TimeSeriesOutput) {
			output.send(Double.parseDouble(value.toString()));
		} else {
			output.send(value);
		}
	}

	/** @return if List is a List of Web3j Types (solidity type + value) */
	public static boolean isWeb3TypeList(List list) {
		if (list == null || list.size() == 0) {
			return false;
		}
		if (list.get(0) instanceof Type) {
			return true;
		}
		if (list.get(0) instanceof List) {
			List inner = (List) list.get(0);
			return isWeb3TypeList(inner);
		}
		return false;
	}

	/**
	 * Format the list as an array of Strings
	 * TODO: should instead convert into List of Streamr types (nested Lists, Maps, numbers, strings, booleans)
	 *
	 * @param l list of web3j Types (solidity type + value)
	 * @return list of Strings
	 */
	public static List<String> convertList(List<Type> l) {
		ArrayList<String> printed = new ArrayList<String>(l.size());
		for (Object o : l) {
			printed.add(typeToString(o));
		}
		return printed;
	}

	public static String typeToString(Object o) {
		if (o instanceof List) {
			StringBuilder sb = new StringBuilder();
			List l = (List) o;
			sb.append("[");
			int i = 0;
			for (Object li : l) {
				if (i++ > 0) {
					sb.append(",");
				}
				sb.append(typeToString(li));
			}
			sb.append("]");
			return sb.toString();
		} else if (o instanceof Type) {
			return ((Type) o).getValue().toString();
		} else {
			return o.toString();
		}
	}
}
