package com.unifina.api

import com.unifina.domain.marketplace.Product
import spock.lang.Specification

class StalenessCheckSpec extends Specification {

	static class Impl implements StalenessCheck {}

	void "isStale() works as expected"() {
		Product product = new Product(blockNumber: 50415, blockIndex: 101)

		expect: "blockNumber < product.blockNumber"
		new Impl(blockNumber: 50414, blockIndex: 100).isStale(product) == true
		new Impl(blockNumber: 50414, blockIndex: 101).isStale(product) == true
		new Impl(blockNumber: 50414, blockIndex: 102).isStale(product) == true

		and: "blockNumber > product.blockNumber"
		new Impl(blockNumber: 50416, blockIndex: 100).isStale(product) == false
		new Impl(blockNumber: 50416, blockIndex: 101).isStale(product) == false
		new Impl(blockNumber: 50416, blockIndex: 102).isStale(product) == false

		and: "blockNumber == product.blockNumber"
		new Impl(blockNumber: 50415, blockIndex: 100).isStale(product) == true
		new Impl(blockNumber: 50415, blockIndex: 101).isStale(product) == true
		new Impl(blockNumber: 50415, blockIndex: 102).isStale(product) == false
	}
}
