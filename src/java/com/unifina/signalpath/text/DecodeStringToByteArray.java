package com.unifina.signalpath.text;

import com.unifina.signalpath.*;
import javax.xml.bind.DatatypeConverter;
import java.util.*;

public class DecodeStringToByteArray extends AbstractSignalPathModule {

	private StringInput in = new StringInput(this, "in");
	private ByteArrayOutput out = new ByteArrayOutput(this, "out");
	private StringOutput error = new StringOutput(this, "error");
	private DecodeParameter decodeType = new DecodeParameter(this, "decodeType", DecodeParameter.BASE64);

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
				case DecodeParameter.HEX: out.send(DatatypeConverter.parseHexBinary(in.getValue()));
					break;
				default:
				case DecodeParameter.BASE64: out.send(DatatypeConverter.parseBase64Binary(in.getValue()));
					break;
			}
		} catch (Exception e ){
			error.send("Failed to parse : '" + in.getValue() + "'");
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
