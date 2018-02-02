package com.unifina.signalpath.blockchain;

import com.unifina.crypto.ECRecover;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;
import org.apache.commons.codec.DecoderException;

import java.security.SignatureException;

public class VerifySignature extends AbstractSignalPathModule {

	private final StringInput message = new StringInput(this, "message");
	private final StringInput signature = new StringInput(this, "signature");
	private final StringOutput address = new StringOutput(this, "address");
	private final StringOutput error = new StringOutput(this, "error");

	@Override
	public void sendOutput() {
		try {
			byte[] messageHash = ECRecover.calculateMessageHash(message.getValue());
			String a = ECRecover.recoverAddress(messageHash, signature.getValue());
			address.send(a);
		} catch (SignatureException | DecoderException e) {
			error.send(e.getMessage());
		}
	}

	@Override
	public void clearState() {}
}
