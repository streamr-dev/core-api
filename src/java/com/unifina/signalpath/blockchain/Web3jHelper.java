package com.unifina.signalpath.blockchain;

import org.apache.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Web3jHelper {
	private static final Logger log = Logger.getLogger(Web3jHelper.class);
	protected static Pattern ARRAY_SUFFIX = Pattern.compile("\\[(\\d*)\\]$");

	public static Function encodeFunction(String fnname, List<String> solidity_inputtypes, List<Object> arguments, List<String> solidity_output_types) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		List<Type> encoded_input = new ArrayList<>();
		Iterator argit = arguments.iterator();
		for (String st : solidity_inputtypes) {
			encoded_input.add(instantiateType(st, argit.next()));
		}
		List<TypeReference<?>> encoded_output = new ArrayList<>();
		for (String st : solidity_output_types) {
			encoded_output.add(TypeReference.create(getTypeClass(st)));
		}
		return new Function(fnname, encoded_input, encoded_output);
	}

	protected static Class getTypeClass(String type) throws ClassNotFoundException {
		Matcher m = ARRAY_SUFFIX.matcher(type);
		if (m.find()) {
			String digits = m.group(1);
			if (digits == null || digits.equals("")) {
				return DynamicArray.class;
			} else {
				return Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + digits);
			}
		}
		switch (type) {
			case "int":
				return Int.class;
			case "uint":
				return Uint.class;
		}
		Class c = AbiTypes.getType(type);
		return c;
	}

	public static TransactionReceipt getTransactionReceipt(Web3j web3, String txhash) throws IOException {
		EthGetTransactionReceipt gtr = web3.ethGetTransactionReceipt(txhash).send();
		if (gtr == null) {
			log.error("TransactionHash not found: " + txhash);
			return null;
		}
		TransactionReceipt tr = gtr.getResult();
		if (tr == null) {
			log.error("TransactionReceipt not found for txhash: " + txhash);
			return null;
		}
		return tr;
	}

	public static EventValues extractEventParameters(Event event, Log log) {
		List<String> topics = log.getTopics();

		String encodedEventSignature = EventEncoder.encode(event);
		if (!topics.get(0).equals(encodedEventSignature)) { return null; }

		List<Type> nonIndexedValues = FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());

		// TODO: handle param/value list length mismatch (this means ABI is wrong/outdated). The module behaviour should be cut to shorter list and keep running. Add test.
		List<Type> indexedValues = new ArrayList<>();
		List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
		for (int i = 0; i < indexedParameters.size(); i++) {
			Type value = FunctionReturnDecoder.decodeIndexedValue(topics.get(i + 1), indexedParameters.get(i));
			indexedValues.add(value);
		}
		return new EventValues(indexedValues, nonIndexedValues);
	}

	public static List<EventValues> extractEventParameters(
		Event event, TransactionReceipt transactionReceipt) {
		List<Log> logs = transactionReceipt.getLogs();
		List<EventValues> values = new ArrayList<>();
		for (Log log : logs) {
			EventValues eventValues = extractEventParameters(event, log);
			if (eventValues != null) {
				values.add(eventValues);
			}
		}
		return values;
	}

	public static BigInteger asBigInteger(Object arg) {
		if (arg instanceof BigInteger) {
			return (BigInteger) arg;
		} else if (arg instanceof BigDecimal) {
			return ((BigDecimal) arg).toBigInteger();
		} else if (arg instanceof String) {
			return Numeric.toBigInt((String) arg);
		} else if (arg instanceof byte[]) {
			return Numeric.toBigInt((byte[]) arg);
		} else if (arg instanceof Number) {
			return BigInteger.valueOf(((Number) arg).longValue());
		}
		return null;
	}

	public static Type instantiateType(String solidity_type, Object arg) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Matcher m = ARRAY_SUFFIX.matcher(solidity_type);
		if (m.find()) {
			return instantiateType(getTypeClass(solidity_type), arg, false, -1);
		}
		String digits = m.group(1);
		//dynamic array (-1) or sized array:
		int array_size = digits == null || digits.equals("") ? -1 : Integer.parseInt(digits);
		return instantiateType(getTypeClass(solidity_type.substring(0, solidity_type.length() - m.group(0).length())), arg, true, array_size);
	}

	/**
	 * @param type
	 * @param arg
	 * @param isArray
	 * @param arraySize if isArray and arraySize > 0, static array will be allocated instead of dynamic array
	 * @return
	 * @throws NoSuchMethodException if Class.getConstructor fails (if constructor not found, means there may be something wrong with generated web3j types)
	 * @throws IllegalAccessException if Constructor.newInstance fails (no public constructor, shouldn't happen)
	 * @throws InvocationTargetException if the function call through Constructor.newInstance throws (if throws, something is wrong with generated web3j types)
	 * @throws InstantiationException if Constructor.newInstance fails because class can't be instantiated (check web3j types)
	 * @throws ClassNotFoundException if org.web3j.abi.datatypes.generated.StaticArrayNNN not found for NNN = arraysize (check arraysize, then web3j types)
	 */

	public static Type instantiateType(Class type, Object arg, boolean isArray, int arraySize) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
		if (type == null || !Type.class.isAssignableFrom(type)) {
			throw new ClassCastException("Class " + type + " must be a subclass of web3j Type");
		}
		if (Array.class.isAssignableFrom(type)) {
			throw new ClassCastException("Instead of passing Array, pass the raw type and isArray = true");
		}

		if (isArray) {
			if (!(arg instanceof List)) {
				throw new ClassCastException("Arg of type " + arg.getClass() + " should be a list to instantiate web3j Array");
			}
			List arglist = (List) arg;
			Constructor listcons;
			if (arraySize <= 0) {
				listcons = DynamicArray.class.getConstructor(new Class[]{Class.class, List.class});
			} else {
				Class arrayClass = Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + arraySize);
				listcons = arrayClass.getConstructor(new Class[]{Class.class, List.class});
			}
			//create a list of transformed arguments
			ArrayList transformedList = new ArrayList(arglist.size());
			for (Object o : arglist) {
				transformedList.add(instantiateType(type, o, false, -1));
			}
			return (Type) listcons.newInstance(type, transformedList);
		}

		Object constructorArg = null;
		if (NumericType.class.isAssignableFrom(type)) {
			constructorArg = asBigInteger(arg);
		} else if (BytesType.class.isAssignableFrom(type)) {
			if (arg instanceof byte[]) {
				constructorArg = arg;
			} else if (arg instanceof BigInteger) {
				constructorArg = ((BigInteger) arg).toByteArray();
			} else if (arg instanceof String) {
				constructorArg = Numeric.hexStringToByteArray((String) arg);
			}
		} else if (Utf8String.class.isAssignableFrom(type)) {
			constructorArg = arg.toString();
		} else if (Address.class.isAssignableFrom(type)) {
			constructorArg = arg.toString();
		} else if (Bool.class.isAssignableFrom(type)) {
			if (arg instanceof Boolean) {
				constructorArg = arg;
			} else {
				BigInteger bival = asBigInteger(arg);
				constructorArg = bival == null ? null : !bival.equals(BigInteger.ZERO);
			}
		}
		if (constructorArg == null) {
			throw new RuntimeException("Could not create type " + type + " from arg " + arg.toString() + " of type " + arg.getClass());
		}
		Constructor cons = type.getConstructor(new Class[]{constructorArg.getClass()});
		return (Type) cons.newInstance(constructorArg);
	}
}
