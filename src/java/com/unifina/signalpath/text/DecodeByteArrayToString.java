package com.unifina.signalpath.text;

import com.unifina.signalpath.*;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

public class DecodeByteArrayToString extends AbstractSignalPathModule {

	private DecodeParameter decodeType = new DecodeParameter(this, "decodeType", DecodeParameter.HEX);
	private ByteArrayInput in = new ByteArrayInput(this, "in");
	private StringOutput error = new StringOutput(this, "error");
	private StringOutput out = new StringOutput(this, "out");

	@Override
	public void init() {
		addInput(in);
		addInput(decodeType);
		addOutput(out);
		addOutput(error);
		in.setCanConnect(true);
		decodeType.setCanConnect(false);
	}

	@Override
	public void sendOutput() {
		try {
			switch (decodeType.getValue()) {
				case DecodeParameter.BASE64:
					out.send(DatatypeConverter.printBase64Binary(in.getValue()));
					break;
				case DecodeParameter.HEX:
				default:
					out.send(DatatypeConverter.printHexBinary(in.getValue()));
					break;
			}
		} catch (IllegalArgumentException e) {
			error.send("Input was null");
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
		protected List<PossibleValue> getPossibleValues() {
			return Arrays.asList(
				new PossibleValue("base64", BASE64),
				new PossibleValue("hex", HEX)
			);
		}
	}
}
