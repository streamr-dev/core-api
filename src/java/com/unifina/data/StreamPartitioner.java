package com.unifina.data;

import com.google.common.hash.HashFunction;
import com.unifina.domain.data.Stream;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;

public class StreamPartitioner {

	private static HashFunction murmur3_32 = com.google.common.hash.Hashing.murmur3_32(0);
	private static final Charset utf8 = Charset.forName("UTF-8");

	public static int partition(Stream stream, @Nullable String partitionKey) {
		return partition(stream.getPartitions(), partitionKey != null ? partitionKey.getBytes(utf8) : null);
	}

	public static int partition(int partitionCount, @Nullable byte[] partitionKey) {
		if (partitionCount == 1) {
			// Fast common case
			return 0;
		} else if (partitionKey != null) {
			byte[] result = murmur3_32.newHasher()
					.putBytes(partitionKey)
					.hash()
					.asBytes();

			// Big-endian interpretation of the result as int
			int intHash = ((result[0] & 0xFF) << 24) | ((result[1] & 0xFF) << 16) | ((result[2] & 0xFF) << 8) | (result[3] & 0xFF);
			return Math.abs(intHash) % partitionCount;
		} else {
			// Fallback to random partition if no key
			return ThreadLocalRandom.current().nextInt(partitionCount);
		}
	}
}
