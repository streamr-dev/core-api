package com.unifina.signalpath.blockchain;

import org.apache.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
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
import java.util.Collections;
import java.util.List;

public class Web3jHelper {
	private static final Logger log = Logger.getLogger(Web3jHelper.class);

	public static Class getTypeClass(String type) {
		if(type.endsWith("[]")){
			return Array.class;
		}
		switch(type){
			case "int":return Int.class;
			case "uint":return Uint.class;
		}
		Class c = AbiTypes.getType(type);
		return c;
	}
	public static TransactionReceipt getTransactionReceipt(Web3j web3, String txhash) throws IOException {
		EthGetTransactionReceipt gtr = web3.ethGetTransactionReceipt(txhash).send();
		if(gtr == null) {
			log.error("TransactionHash not found: " + txhash);
			return null;
		}
		TransactionReceipt tr = gtr.getResult();
		if(tr == null) {
			log.error("TransactionReceipt not found for txhash: " + txhash);
			return null;
		}
		return tr;
	}

	public static EventValues extractEventParameters(
			Event event, Log log) {

		List<String> topics = log.getTopics();
		String encodedEventSignature = EventEncoder.encode(event);
		if (!topics.get(0).equals(encodedEventSignature)) {
			return null;
		}

		List<Type> indexedValues = new ArrayList<>();
		List<Type> nonIndexedValues = FunctionReturnDecoder.decode(
				log.getData(), event.getNonIndexedParameters());

		List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
		for (int i = 0; i < indexedParameters.size(); i++) {
			Type value = FunctionReturnDecoder.decodeIndexedValue(
					topics.get(i + 1), indexedParameters.get(i));
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

	public static BigInteger asBigInteger(Object arg){
		if(arg instanceof BigInteger)
			return (BigInteger) arg;
		else if(arg instanceof BigDecimal) {
			return ((BigDecimal) arg).toBigInteger();
		}
		else if(arg instanceof String) {
			return Numeric.toBigInt((String) arg);
		}
		else if(arg instanceof Number)
			return BigInteger.valueOf(((Number) arg).longValue());
		return null;
	}

	public static Type instantiateType(String solidity_type, Object arg) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		return instantiateType(getTypeClass(solidity_type),arg);
	}


	public static Type instantiateType(Class type, Object arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if(type == null || !Type.class.isAssignableFrom(type))
			throw new ClassCastException("Class "+type+" must be a subclass of web3j Type");

		if(Array.class.isAssignableFrom(type)) {
			if(!(arg instanceof List))
				throw new ClassCastException("Arg of type "+arg.getClass()+" should be a list to instantiate web3j Array Type");
			List arglist = (List) arg;
			Constructor listcons = type.getConstructor(new Class[]{List.class});
			//instantiate a sample of the array to see what class it holds
			Array sample = (Array) listcons.newInstance(Collections.EMPTY_LIST);
			Class valueclass = sample.getClass();
			//create a list of transformed arguments
			ArrayList transformedList = new ArrayList(arglist.size());
			for(Object o : arglist){
				transformedList.add(instantiateType(valueclass,o));
			}
			return (Type) listcons.newInstance(transformedList);
		}


		Object constructorArg = null;
		if(NumericType.class.isAssignableFrom(type)){
			constructorArg = asBigInteger(arg);
		}
		else if(BytesType.class.isAssignableFrom(type)) {
			if(arg instanceof byte[])
				constructorArg = arg;
			else if(arg instanceof BigInteger) {
				constructorArg = ((BigInteger) arg).toByteArray();
			}
			else if(arg instanceof String){
				constructorArg = Numeric.hexStringToByteArray((String) arg);
			}
		}
		else if(Utf8String.class.isAssignableFrom(type)) {
			constructorArg = arg.toString();
		}
		else if(Address.class.isAssignableFrom(type)) {
			constructorArg = arg.toString();
		}
		else if(Bool.class.isAssignableFrom(type)) {
			if(arg instanceof Boolean){
				constructorArg = arg;
			}
			else {
				BigInteger bival = asBigInteger(arg);
				constructorArg = bival == null ? null : !bival.equals(BigInteger.ZERO);
			}
		}
		if(constructorArg == null){
			throw new RuntimeException("Could not create type "+type+" from arg "+arg.toString()+" of type "+arg.getClass());
		}
		Constructor cons = type.getConstructor(new Class[]{constructorArg.getClass()});
		return (Type) cons.newInstance(constructorArg);
	}
}
