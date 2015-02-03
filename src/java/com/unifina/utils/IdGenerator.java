package com.unifina.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class IdGenerator {
	
	/**
	 * Returns an URL-safe base64 encoding of a randomly generated UUID
	 * @return
	 */
	public static String get() {
		UUID uuid = UUID.randomUUID();
		
		byte[] bytes = new byte[16];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		
		return Base64.encodeBase64URLSafeString(bytes);
	}
	
}
