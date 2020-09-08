package com.unifina.service

import com.unifina.domain.Product

trait StalenessCheck {
	Long blockNumber
	Long blockIndex

	/**
	 * Determines whether the data contained in this command is stale (older) compared to what is known in Product.
	 */
	boolean isStale(Product product) {
		blockNumber == product.blockNumber ? blockIndex <= product.blockIndex : blockNumber < product.blockNumber
	}
}
