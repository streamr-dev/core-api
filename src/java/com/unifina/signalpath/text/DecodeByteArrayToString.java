package com.unifina.signalpath.text;

import com.unifina.signalpath.*;
import javax.xml.bind.DatatypeConverter;
import java.util.*;

public class DecodeByteArrayToString extends AbstractSignalPathModule {

	private ByteArrayInput in = new ByteArrayInput(this, "in");
	private StringOutput out = new StringOutput(this, "out");
	private StringOutput error = new StringOutput(this, "error");
	private DecodeParameter decodeType = new DecodeParameter(this, "decodeType", DecodeParameter.HEX);

	@Override
	public void init() {
		addInput(in);
		addInput(decodeType);
		addOutput(out);
		addOutput(error);
		in.setCanConnect(true);
		decodeType.setCanConnect(false);
		decodeType.setUpdateOnChange(true);
	}

	@Override
	public void sendOutput() {

		try {
			switch (decodeType.getValue()){
				case DecodeParameter.BASE64: out.send(DatatypeConverter.printBase64Binary(in.getValue()));
					break;
				default:
				case DecodeParameter.HEX: out.send(DatatypeConverter.printHexBinary(in.getValue()));
					break;
			}
		} catch (Exception e ){
			error.send("Failed to parse byte array");
		}
	}

	@Override
	public void clearState() {

	}

	public static class DecodeParameter extends StringParameter {

		public static final String BASE64 = "base64";
		public static final String HEX = "hex";

		public DecodeParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		protected List<PossibleValue> getPossibleValues(){
			return Arrays.asList(
					new PossibleValue("base64", BASE64),
					new PossibleValue("hex", HEX)
			);
		}
	}
}
