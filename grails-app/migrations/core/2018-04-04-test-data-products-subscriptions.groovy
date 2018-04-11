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
	id: "b58a3388b08a4bfd84b96fa8b3a4c43d58eb1d11525a42c49ee908ee37143811",
	name: "Air Quality Index (pm10)",
	img: "https://images.pexels.com/photos/158904/pexels-photo-158904.jpeg"
], [
	id: "eb25056c5f504bd29a221f2fe75229e7e10bb10c467b45bf8a7b5a45b648e9d5",
	name: "Flower pollination",
	img: "https://images.pexels.com/photos/22455/pexels-photo.jpg"
], [
	id: "ead9408758e2423da60631b38f37607c4156f86bea334344bc519143bd9a113d",
	name: "Rail road traffic conditions",
	img: "https://images.pexels.com/photos/673803/pexels-photo-673803.jpeg"
], [
	id: "237cb7d218c245a0934b897f1fbe25b97790bf1c172e46b5901e95081c724fff",
	name: "Bike traffic conditions",
	img: "https://images.pexels.com/photos/250674/pexels-photo-250674.jpeg"
], [
	id: "2a90dd37ac964d40845952e12473af4bfb26afa3b6014752a8ca424fcfdfe5d3",
	name: "Glacial snow status in Alps",
	img: "https://images.pexels.com/photos/273040/pexels-photo-273040.jpeg"
], [
	id: "a05367e293f144cc9a2024ae2eab9574369d9449c49d46bebb5ec7cb7438222e",
	name: "Weather on North Atlantic",
	img: "https://images.pexels.com/photos/744515/pexels-photo-744515.jpeg"
], [
	id: "bc6bb331952349508c51417a8c23de74c7e736d68fec49519816a0daeb720fc3",
	name: "Credit card transactions",
	img: "https://images.pexels.com/photos/164501/pexels-photo-164501.jpeg"
], [
	id: "dc6129c3b3d345d593f741c42cc6fc9f713e5d6a21fe4ee88dee8024af8baa07",
	name: "Endangered species tracking sensors",
	img: "https://images.pexels.com/photos/772997/pexels-photo-772997.jpeg"
], [
	id: "2678f8a68a854b9fbeeb926a0dd63d7bf6a6d8a9c3c34ee3b7d8edb98efe080d",
	name: "Amusement park ride maintenance condition sensors",
	img: "https://images.pexels.com/photos/772449/pexels-photo-772449.jpeg"
], [
	id: "7a11acc365214088a20d4ae87286abe050f0efa29ad0486387caf0c44bea2740",
	name: "Fruit ripeness sensors",
	img: "https://images.pexels.com/photos/701969/pexels-photo-701969.jpeg"
], [
	id: "a7e0a6229ca542aeb368b65f051df0fc8d18776b929f415093a75db0379a91a2",
	name: "Glacial snow status in the Rocky Mountains",
	img: "https://images.pexels.com/photos/301558/pexels-photo-301558.jpeg"
], [
	id: "c8bba0be7e244d4190edd765a47b1c6824219be5d8ef4297948cdffe56376a22",
	name: "Ride-hailing vehicle tracking data",
	img: "https://images.pexels.com/photos/36853/pretty-woman-traffic-young-vintage.jpg"
], [
	id: "e60b8e5e04b04a99a32d4452d18f7685e1b11156d6874bbf8af2a43e92f1d093",
	name: "CO2 atmospheric sensors",
	img: "https://images.pexels.com/photos/459670/pexels-photo-459670.jpeg"
], [
	id: "eaf575100b7c4cf597ca8aef706273784fb4175b52b24bb48dfa971dbca2de27",
	name: "People Flow(TM) sensors",
	img: "https://images.pexels.com/photos/275286/pexels-photo-275286.jpeg"
], [
	id: "f7a9ca2193bc439aa00222fa25d45b3e7f0d2ab779b24f82930cb00a2356d1fa",
	name: "Plastic packages recycling data stream",
	img: "https://images.pexels.com/photos/802221/pexels-photo-802221.jpeg"
], [
	id: "504bd29a221f2e5d45b3e7f056c5f504bd29a221f2fe75229e7b5a45b648e9d5",
	name: "Brightness of stars visible in the Northern hemisphere",
	img: "https://images.pexels.com/photos/62385/night-stars-sky-trees-62385.jpeg"
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
			// between 1 cent / month and 10 000 USD / month
			def price, currency
			if (rand(0, 10) < 8) {
				currency = "DATA"
				price = new Long(10) ** rand(7, 11) * rand(1, 10000)
			} else {
				currency = "USD"
				price = new Long(10) ** rand(0, 4) * rand(1, 10000)
			}
			insert(tableName: "product") {
				column(name: "id", value: product.id)
				column(name: "beneficiary_address", value: randomFrom(addresses))
				column(name: "owner_address", value: randomFrom(addresses))
				column(name: "category_id", value: randomFrom(categoryIDs))
				column(name: "image_url", value: product.img + "?h=400")
				column(name: "thumbnail_url", value: product.img + "?h=210")
				column(name: "minimum_subscription_in_seconds", valueNumeric: rand(1, 60))
				column(name: "name", value: product.name)
				column(name: "description", value: "${product.name}. " * rand(3, 7))
				column(name: "preview_config_json")//, value: "{}")	// TODO: what should be in here? Now inserts NULL.
				column(name: "preview_stream_id", value: "ln2g8OKHSdi7BcL-bcnh2g")	// Twitter-Bitcoin, read key "TaPRLN84RXqh8HXuFjQDLg"
				column(name: "price_per_second", valueNumeric: price)
				column(name: "price_currency", value: currency)
				column(name: "state", value: rand(0, 10) < 5 ? "DEPLOYED" : rand(0, 10) < 8 ? "NOT_DEPLOYED" : "DEPLOYING")
				column(name: "block_index", valueNumeric: 1)
				column(name: "block_number", valueNumeric: rand(0, 1000))
				column(name: "score", valueNumeric: 0)
				column(name: "version", valueNumeric: 0)
				column(name: "date_created", value: "2018-01-01 00:00:00")
				column(name: "last_updated", value: "2018-01-01 00:00:00")
			}

			randomSetOf(5, streamIDs).each { streamID ->
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
}
