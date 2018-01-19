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
		List correctResults = [5, 6, 3, 9, 3, 0, 2, 8, 2, 6, 9, 5, 5, 8, 5, 0, 0, 7, 2, 8, 5, 6, 8, 1, 7, 9, 2, 1, 8, 5, 6, 4, 3, 3, 1, 7, 1, 5, 2, 8, 3, 3, 8, 6, 8, 7, 4, 8, 2, 3, 5, 2, 8, 8, 8, 9, 8, 2, 7, 7, 0, 8, 8, 5, 9, 9, 9, 7, 2, 7, 0, 4, 4, 6, 4, 8, 5, 5, 0, 8, 2, 5, 1, 8, 6, 8, 8, 1, 2, 0, 7, 3, 2, 2, 5, 7, 9, 6, 4, 7]
		Stream stream = new Stream()
		stream.partitions = 10

		when:
		List results = keys.collect {partitioner.partition(stream, it)}

		then:
		results == correctResults
	}
}
