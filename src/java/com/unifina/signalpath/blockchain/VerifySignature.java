package com.unifina.signalpath.blockchain;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ModuleWarningMessage;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

import java.security.SignatureException;

public class VerifySignature extends AbstractSignalPathModule {
	private static final String SIGN_MAGIC = "\u0019Ethereum Signed Message:\n";

	private final StringInput message = new StringInput(this, "message");
	private final StringInput signature = new StringInput(this, "signature");
	private final StringOutput address = new StringOutput(this, "address");
	private final StringOutput error = new StringOutput(this, "error");

	@Override
	public void sendOutput() {
		try {
			byte[] messageHash = calculateMessageHash(message.getValue());
			String a = ecRecover(messageHash, signature.getValue());
			address.send(a);
		} catch (SignatureException | DecoderException e) {
			error.send(e.getMessage());
		}
	}

	@Override
	public void clearState() {}

	private static byte[] calculateMessageHash(String message) throws DecoderException {
		String messageHex = "0x" + Hex.encodeHexString(message.getBytes());
		byte[] messageBytes = Hex.decodeHex(messageHex.replace("0x","").toCharArray());
		String prefix = SIGN_MAGIC + messageBytes.length;
		byte[] toHash = ByteUtil.merge(prefix.getBytes(),messageBytes);
		return HashUtil.sha3(toHash);
	}

	private static String ecRecover(byte[] messageHash, String signatureHex) throws SignatureException, DecoderException {
		byte[] signature = Hex.decodeHex(signatureHex.replace("0x", "").toCharArray());

		byte[] r = new byte[32];
		byte[] s = new byte[32];
		byte v = signature[64];
		System.arraycopy(signature, 0, r,0, r.length);
		System.arraycopy(signature, 32, s,0, s.length);

		ECKey.ECDSASignature signatureObj = ECKey.ECDSASignature.fromComponents(r, s, v);
		return "0x" + Hex.encodeHexString(ECKey.signatureToKey(messageHash, signatureObj.toBase64()).getAddress());
	}
}
