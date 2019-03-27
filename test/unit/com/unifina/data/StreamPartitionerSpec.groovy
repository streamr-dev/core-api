package com.unifina.data

import com.unifina.domain.data.Stream
import spock.lang.Specification

class StreamPartitionerSpec extends Specification {

	StreamPartitioner partitioner

	def setup() {
		partitioner = new StreamPartitioner()
	}

	void "partition() produces the expected partitioning"() {
		List<String> keys = (0..99).collect { "key-$it" }
		// Results must be the same as those produced by streamr-http-api/lib/partitioner.js
		List correctResults = [6, 7, 4, 4, 9, 1, 8, 0, 6, 6, 7, 6, 7, 3, 2, 2, 0, 9, 4, 9, 9, 5, 5, 1, 7, 3,
							   0, 6, 5, 6, 3, 6, 3, 5, 6, 2, 3, 6, 7, 2, 1, 3, 2, 7, 1, 1, 5, 1, 4, 0, 1, 9,
							   7, 4, 2, 3, 2, 9, 7, 7, 4, 3, 5, 4, 5, 3, 9, 0, 4, 8, 1, 7, 4, 8, 1, 2, 9, 9,
							   5, 3, 5, 0, 9, 4, 3, 9, 6, 7, 8, 6, 4, 6, 0, 1, 1, 5, 8, 3, 9, 7]
		Stream stream = new Stream()
		stream.partitions = 10

		when:
		List results = keys.collect {partitioner.partition(stream, it)}

		then:
		results == correctResults
	}
}
