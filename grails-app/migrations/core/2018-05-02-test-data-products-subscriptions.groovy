package core

// ids from test database (so they're existing streams)
def categoryIDs = ["ad-id",
				   "automobile-id",
				   "cryptocurrencies-id",
				   "financial-id",
				   "personal-id",
				   "satellite-id"]
def streamIDs =  ["4jFT4_yRSFyElSj9pHmovg",
				  "4nxQHjdNQVmy551UB6S4cQ",
				  "c1_fiG6PTxmtnCYGU-mKuQ",
				  "IIkpufIYSBu9_Kfot2e78Q",
				  "JFXhMJjCQzK-SardC8faXQ",
				  "ln2g8OKHSdi7BcL-bcnh2g",
				  "mvGKMdDrTeaij6mmZsQliA",
				  "pltRMd8rCfkij4mlZsQkJB",
				  "RUj6iJggS3iEKsUx5C07Ig",
				  "run-canvas-spec",
				  "share-spec-stream-uuid",
				  "YpTAPDbvSAmj-iCUYz-dxA"]
def addresses = ["0x725bf47f71061034757b37cc7b9f73671c7b2973",
				 "0x66c595baf661c8dfcebc50dd431b727246d748d4",
				 "0xdb0ada416674557aa697cf33d261ce02d4016776",
				 "0x72cf0d1ac81571a6cf767fd649ec95f5e12da541",
				 "0x942e694ec12d009f45aead2563426adc182ff527",
				 "0x23cdc37931c4142ec6c326326d59db37a27fc354",
				 "0x996f0e99758d8fd32d196243fa178a95a6c71784",
				 "0x993b0c35a9474b5d99fa7302024932bc4ed54d3c",
				 "0x4e702165bc042e38b4e22653751e49d40ed9e732",
				 "0x247c7ffcc5f9d3c46eb4621c1bf68e11eb75ac01"]

// dummy products for marketplace tests
// images from https://www.pexels.com/royalty-free-images/
//   licensed under the Creative Commons Zero (CC0) license https://creativecommons.org/publicdomain/zero/1.0/
products = [[
	name: "Air Quality Index (pm10)",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-158904.jpeg"
], [
	name: "Flower pollination",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo.jpg"
], [
	name: "Rail road traffic conditions",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-673803.jpeg"
], [
	name: "Bike traffic conditions",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-250674.jpeg"
], [
	name: "Glacial snow status in Alps",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-273040.jpeg"
], [
	name: "Weather on North Atlantic",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-744515.jpeg"
], [
	name: "Credit card transactions",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-164501.jpeg"
], [
	name: "Endangered species tracking sensors",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-772997.jpeg"
], [
	name: "Amusement park ride maintenance condition sensors",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-772449.jpeg"
], [
	name: "Fruit ripeness sensors",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-701969.jpeg"
], [
	name: "Glacial snow status in the Rocky Mountains",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-301558.jpeg"
], [
	name: "Ride-hailing vehicle tracking data",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pretty-woman-traffic-young-vintage.jpg"
], [
	name: "CO2 atmospheric sensors",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-459670.jpeg"
], [
	name: "People Flow(TM) sensors",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-275286.jpeg"
], [
	name: "Plastic packages recycling data stream",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/pexels-photo-802221.jpeg"
], [
	name: "Brightness of stars visible in the Northern hemisphere",
	img: "https://s3-eu-west-1.amazonaws.com/streamr-dev-public/product-images/test-hero-images/night-stars-sky-trees-62385.jpeg"
]]

// deterministic pseudorandom numbers and stream sets
long generatorState = 0
def rand = { min, max ->
	generatorState = (generatorState * 1337 + 137) % 31337
	min + generatorState % (max - min + 1) as int
}
def randomFrom = { list ->
	list[rand(0, list.size() - 1)]
}
def randomSetOf = { maxCount, list ->
	list[(1..rand(1, maxCount)).collect { rand(0, list.size() - 1) } as Set]
}

databaseChangeLog = {
	changeSet(author: "juuso", id: "test-data-products-subscriptions", context: "test") {
		products.each { product ->
			product.id = "0123457689abcdef"[(1..64).collect({ rand(0, 15) })]
			def product_streams = randomSetOf(5, streamIDs)
			boolean isFreeProduct = rand(0, 5) < 3

			insert(tableName: "product") {
				column(name: "id", value: product.id)
				column(name: "beneficiary_address", value: randomFrom(addresses))
				column(name: "owner_address", value: randomFrom(addresses))
				column(name: "category_id", value: randomFrom(categoryIDs))
				column(name: "image_url", value: product.img)
				column(name: "thumbnail_url", value: null) // note: thumbnail_url will be overwritten by 2018-05-23-fix-product-images migration
				column(name: "minimum_subscription_in_seconds", valueNumeric: rand(1, 600))
				column(name: "name", value: product.name)
				column(name: "description", value: "${product.name}. " * rand(3, 7))
				column(name: "preview_config_json")//, value: "{}")	// TODO: what should be in here? Now inserts NULL.
				column(name: "preview_stream_id", value: product_streams[0])
				column(name: "state", value: rand(0, 10) < 5 ? "DEPLOYED" : rand(0, 10) < 8 ? "NOT_DEPLOYED" : "DEPLOYING")
				column(name: "block_index", valueNumeric: 1)
				column(name: "block_number", valueNumeric: rand(0, 1000))
				column(name: "score", valueNumeric: 0)
				column(name: "version", valueNumeric: 0)
				column(name: "date_created", value: "2018-01-01 00:00:00")
				column(name: "last_updated", value: "2018-01-01 00:00:00")
				column(name: "owner_id", value: rand(1, 3)) // tester one/two/admin

				if (isFreeProduct) {
					column(name: "price_per_second", valueNumeric: 0)
					column(name: "price_currency", value: "DATA")
				} else {
					// between 1 cent / month and 10 000 USD / month
					column(name: "price_per_second", valueNumeric: new Long(10) ** rand(0, 4) * rand(1, 10000))
					column(name: "price_currency", value: rand(0, 10) < 7 ? "DATA" : "USD")
				}
			}

			product_streams.each { streamID ->
				insert(tableName: "product_streams") {
					column(name: "product_id", value: product.id)
					column(name: "stream_id", value: streamID)
				}
			}

			randomSetOf(3, addresses).each { address ->
				insert(tableName: "subscription") {
					column(name: "product_id", value: product.id)
					column(name: "address", value: address)
					column(name: "ends_at", value: "${rand(2018, 2020)}-0${rand(1, 9)}-${rand(10, 28)} 13:37:00")
					column(name: "version", valueNumeric: 0)
					column(name: "date_created", value: "2018-01-01 00:00:00")
					column(name: "last_updated", value: "2018-01-01 00:00:00")
					column(name: "class", value: isFreeProduct ? "com.unifina.domain.marketplace.FreeSubscription" : "com.unifina.domain.marketplace.PaidSubscription")
				}
			}
		}
	}

	changeSet(author: "juuso", id: "test-data-products-permissions", context: "test") {
		products.each { product ->
			["read", "write", "share"].each { operation ->
				insert(tableName: "permission") {
					column(name: "version", valueNumeric: 0)
					column(name: "product_id", value: product.id)
					column(name: "operation", value: operation)
					column(name: "user_id", valueNumeric: 1)	// tester1
					column(name: "anonymous", valueNumeric: 0)
				}
			}
		}
	}

	changeSet(author: "juuso", id: "test-data-products-public-read-permissions", context: "test") {
		products.each { product ->
			insert(tableName: "permission") {
				column(name: "version", valueNumeric: 0)
				column(name: "product_id", value: product.id)
				column(name: "operation", value: "read")
				column(name: "anonymous", valueNumeric: 1)
			}
		}
	}
}
