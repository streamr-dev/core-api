package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream

class ProductStream implements Serializable {
	Product product
	Stream stream

	static mapping = {
		id composite: ['product', 'stream']
		version false
	}
}
