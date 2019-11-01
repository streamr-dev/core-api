package com.unifina.utils;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

public class HexIdGenerator implements IdentifierGenerator {

	/**
	 * Returns a hex string of 32 random bytes (64 hex characters)
	 * @return String
	 */
	@Override
	public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
		UUID uuid = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();

		byte[] bytes = new byte[32];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		bb.putLong(uuid2.getMostSignificantBits());
		bb.putLong(uuid2.getLeastSignificantBits());

		return new String(Hex.encode(bytes));
	}

}
