package com.streamr.core.service

import com.streamr.core.domain.Category
import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

import static plastic.criteria.PlasticCriteria.mockCriteria

@Mock([Product, User, Category, Permission])
class ProductListParamsSpec extends Specification {

	Category c1, c2, c3, c4
	User user, other

	void setup() {
		String address = "0x808a9b387Cf830CEb16108C84b50202a69188fdd"
		user = new User(
			username: address,
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		other = new User(
			username: "usr@foo.com",
			name: "Firstname Lastname",
		)
		other.id = 2
		other.save(failOnError: true, validate: false)

		c1 = new Category(name: "category-1")
		c2 = new Category(name: "category-2")
		c3 = new Category(name: "category-3")
		c4 = new Category(name: "category-4")
		[c1, c2, c3, c4]*.save(failOnError: true, validate: true)

		Product p1 = new Product(
			name: "Generic Product",
			description: "Hello, world! I am a product.",
			ownerAddress: address,
			beneficiaryAddress: address,
			category: c1,
			pricePerSecond: "0",
			state: Product.State.NOT_DEPLOYED,
			owner: user
		)
		Product p2 = new Product(
			name: "Hello Product",
			description: "description",
			ownerAddress: address,
			beneficiaryAddress: address,
			category: c2,
			pricePerSecond: "10",
			state: Product.State.DEPLOYING,
			owner: user
		)
		Product p3 = new Product(
			name: "Cryptocurrency Product",
			description: "Live exchange rate between USD and ETH",
			ownerAddress: address,
			beneficiaryAddress: address,
			category: c3,
			pricePerSecond: "1",
			state: Product.State.DEPLOYED,
			owner: user
		)
		Product p4 = new Product(
			name: "Automobile Product",
			description: "Real-time automobile sensor and GPS data",
			ownerAddress: address,
			beneficiaryAddress: address,
			category: c1,
			pricePerSecond: "3",
			state: Product.State.DEPLOYED,
			owner: other
		)
		Product p5 = new Product(
			name: "Water Product",
			description: "Water quality",
			ownerAddress: address,
			beneficiaryAddress: address,
			category: c4,
			pricePerSecond: "11",
			state: Product.State.DEPLOYED,
			type: Product.Type.DATAUNION,
			owner: user,
		)

		mockCriteria(Product) // support for criteria `in`

		[p1, p2, p3, p4, p5].eachWithIndex { Product p, int i -> p.id = "product-${i + 1}" }
		// Assign ids: product-1, ...

		[p1, p2, p3, p4, p5]*.save(failOnError: true, validate: true)
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
		map              | numOfErrors | fieldsWithError
		[categories: []] | 1           | ["categories"]
		[states: []]     | 1           | ["states"]
	}

	void "createListCriteria() with empty-args constructor returns criteria that returns all"() {
		when:
		def paramsList = new ProductListParams()
		then:
		fetchProductIdsFor(paramsList).size() == 5
	}

	void "createListCriteria() with search arg returns criteria that searches through name, description"() {
		when:
		def paramsList = new ProductListParams(search: "Hello")
		then:
		fetchProductIdsFor(paramsList) == ["product-1", "product-2"] as Set
	}

	void "createListCriteria() with free=true returns criteria that returns only free products"() {
		when:
		def paramsList = new ProductListParams(free: true)
		then:
		fetchProductIdsFor(paramsList) == ["product-1"] as Set
	}

	void "createListCriteria() with free=false returns criteria that returns only non-free products"() {
		when:
		def paramsList = new ProductListParams(free: false)
		then:
		fetchProductIdsFor(paramsList) == ["product-2", "product-3", "product-4", "product-5"] as Set
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
		fetchProductIdsFor(paramsList) == ["product-1", "product-2", "product-3", "product-5"] as Set
	}

	void "createListCriteria() with owner returns criteria that filters products by type"() {
		when:
		def paramsList = new ProductListParams(productOwner: user)
		paramsList.type = Product.Type.DATAUNION
		then:
		fetchProductIdsFor(paramsList) == ["product-5"] as Set
	}

	private static Set<String> fetchProductIdsFor(ListParams listParams) {
		Product.withCriteria(listParams.createListCriteria())*.id
	}

	void "default constructor sets operation"() {
		when:
		ProductListParams params = new ProductListParams()
		then:
		params.operationToEnum() == Permission.Operation.PRODUCT_GET
	}

	void "map constructor sets operation"() {
		when:
		ProductListParams params = new ProductListParams([:])
		then:
		params.operationToEnum() == Permission.Operation.PRODUCT_GET
	}

	void "map constructor allows operation override"() {
		when:
		ProductListParams params = new ProductListParams([operation: Permission.Operation.PRODUCT_SHARE.toString()])
		then:
		params.operationToEnum() == Permission.Operation.PRODUCT_SHARE
	}
}
