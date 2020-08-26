package com.unifina.data;

import com.unifina.domain.Stream;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class StreamPartitioner{
	private static final Charset utf8 = Charset.forName("UTF-8");

	public static int partition(Stream stream, @Nullable String partitionKey) {
		return partition(stream.getPartitions(), partitionKey != null ? partitionKey.getBytes(utf8) : null);
	}

	public static int partition(int partitionCount, @Nullable byte[] partitionKey) {
		if (partitionCount == 1) {
			// Fast common case
			return 0;
		} else if (partitionKey != null) {
			int intHash = hash(partitionKey);
			return Math.abs(intHash) % partitionCount;
		} else {
			// Fallback to random partition if no key
			return ThreadLocalRandom.current().nextInt(partitionCount);
		}
	}

	private static int hash(byte[] partitionKey) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return ByteBuffer.wrap(md.digest(partitionKey)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
	}
}
