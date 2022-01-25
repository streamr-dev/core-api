package com.streamr.core.service;

import com.streamr.core.domain.Product;

public class ProductNotFreeException extends ApiException {
	public ProductNotFreeException(Product product) {
		super(400, "PRODUCT_IS_NOT_FREE", String.format("Product %s is not free", product.getId()));
	}
}
