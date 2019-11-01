package com.unifina.api;

import com.unifina.domain.marketplace.Product;

public class ProductNotFreeException extends ApiException {
	public ProductNotFreeException(Product product) {
		super(400, "PRODUCT_IS_NOT_FREE", String.format("Product %s is not free", product.getId()));
	}
}
