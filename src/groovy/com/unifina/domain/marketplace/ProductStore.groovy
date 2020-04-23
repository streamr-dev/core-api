package com.unifina.domain.marketplace

/**
 * Motivation of this class is to
 * <ul>
 *     <li>Encapsulate database access to its own class</li>
 *     <li>Place code that doesn't work with @GrailsCompileStatic to its own class</li>
 * </ul>
 *
 * @see ProductService
 */
class ProductStore extends Store<Product> {
	/**
	 * @see com.unifina.service.ProductService#findProductsForScoring
	 *
	 * @return List of products in ascending order by date created.
	 */
	List<Product> findProductsForScoring() {
		List<Product> products = Product.createCriteria().list() {
			order("dateCreated", "asc")
		}
		return products
	}
}
