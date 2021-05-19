package com.unifina.utils;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

public class IdGenerator implements IdentifierGenerator {
	/**
	 * Returns an URL-safe base64 encoding of a randomly generated UUID
	 *
	 * @return
	 */
	public static String get() {
		return getShort() + getShort();
	}

	public static String getShort() {
		UUID uuid = UUID.randomUUID();

		byte[] bytes = new byte[16];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		return Base64.encodeBase64URLSafeString(bytes);
	}

	public String generate() {
		return get();
	}

	@Override
	public Serializable generate(SessionImplementor session, Object o) {
		return generate();
	}
}
