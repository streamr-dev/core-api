package com.unifina.service;

import com.unifina.domain.Product;

public class ProductNotFreeException extends ApiException {
	public ProductNotFreeException(Product product) {
		super(400, "PRODUCT_IS_NOT_FREE", String.format("Product %s is not free", product.getId()));
	}
}
