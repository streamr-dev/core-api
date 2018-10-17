package com.unifina.api

import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

import static plastic.criteria.PlasticCriteria.*

@Mock(Product)
class ProductListParamsSpec extends Specification {

	Category c1, c2, c3
	SecUser user, other

	void setup() {
		user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		other = new SecUser(
			username: "usr@foo.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		other.id = 2
		other.save(failOnError: true, validate: false)

		c1 = new Category(name: "category-1")
		c2 = new Category(name: "category-2")
		c3 = new Category(name: "category-3")
		[c1, c2, c3]*.save(failOnError: true, validate: true)

		Product p1 = new Product(
			name: "Generic Product",
			description: "Hello, world! I am a product.",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			category: c1,
			pricePerSecond: 5,
			state: Product.State.NOT_DEPLOYED,
			owner: user
		)
		Product p2 = new Product(
			name: "Hello Product",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			category: c2,
			pricePerSecond: 10,
			state: Product.State.DEPLOYING,
			owner: user
		)
		Product p3 = new Product(
			name: "Cryptocurrency Product",
			description: "Live exchange rate between USD and ETH",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			category: c3,
			pricePerSecond: 1,
			state: Product.State.DEPLOYED,
			owner: user
		)
		Product p4 = new Product(
			name: "Automobile Product",
			description: "Real-time automobile sensor and GPS data",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			category: c1,
			pricePerSecond: 3,
			state: Product.State.DEPLOYED,
			owner: other
		)

		mockCriteria(Product) // support for criteria `in`

		[p1, p2, p3, p4].eachWithIndex { Product p, int i -> p.id = "product-${i+1}" } // Assign ids: product-1, ...

		[p1, p2, p3, p4]*.save(failOnError: true, validate: true)
	}

	void "passes validation with no args"() {
		expect:
		new ProductListParams().validate()
	}

	@Unroll
	void "#map does not pass validation"(Map map, int numOfErrors, List<String> fieldsWithError) {
		def params = new ProductListParams(map)

		expect:
		!params.validate()
		params.errors.errorCount == numOfErrors
		params.errors.getFieldErrors()*.field == fieldsWithError

		where:
		map                  | numOfErrors | fieldsWithError
		[minPrice: -1]       | 1           | ["minPrice"]
		[categories: []]     | 1           | ["categories"]
		[states: []]         | 1           | ["states"]
	}

	void "createListCriteria() with empty-args constructor returns criteria that returns all"() {
		when:
		def paramsList = new ProductListParams()
		then:
		fetchProductIdsFor(paramsList).size() == 4
	}

	void "createListCriteria() with search arg returns criteria that searches through name, description"() {
		when:
		def paramsList = new ProductListParams(search: "Hello")
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-2"] as Set
	}

	void "createListCriteria() with minPrice returns criteria that filters products by price"() {
		when:
		def paramsList = new ProductListParams(minPrice: 3)
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-2", "product-4"] as Set
	}

	void "createListCriteria() with maxPrice returns criteria that filters products by price"() {
		when:
		def paramsList = new ProductListParams(maxPrice: 3)
		then:
		fetchProductIdsFor(paramsList) == ["product-3", "product-4"] as Set
	}

	void "createListCriteria() with minPrice and maxPrice returns criteria that filters products by price"() {
		when:
		def paramsList = new ProductListParams(minPrice: 3, maxPrice: 5)
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-4"] as Set
	}

	void "createListCriteria() with categories returns criteria that filters products by category"() {
		when:
		def paramsList = new ProductListParams(categories: [c1, c2])
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-2", "product-4"] as Set
	}

	void "createListCriteria() with states returns criteria that filters products by state"() {
		when:
		def paramsList = new ProductListParams(states: [Product.State.NOT_DEPLOYED, Product.State.DEPLOYING])
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-2"] as Set
	}

	void "createListCriteria() with owner returns criteria that filters products by owner"() {
		when:
		def paramsList = new ProductListParams(productOwner: user)
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-2", "product-3"] as Set
	}

	private static Set<String> fetchProductIdsFor(ListParams listParams) {
		Product.withCriteria(listParams.createListCriteria())*.id
	}
}
